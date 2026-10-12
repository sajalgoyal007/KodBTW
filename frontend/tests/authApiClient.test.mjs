import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

async function importTypeScript(relativePath, dependencyUrls = {}) {
  const sourceUrl = new URL(relativePath, import.meta.url);
  const source = await readFile(sourceUrl, 'utf8');
  const { outputText: originalOutput, diagnostics } = ts.transpileModule(source, {
    compilerOptions: {
      module: ts.ModuleKind.ESNext,
      target: ts.ScriptTarget.ES2022,
    },
    reportDiagnostics: true,
  });

  const errors = diagnostics.filter((diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error);
  assert.deepEqual(errors, [], `TypeScript transpilation failed for ${relativePath}`);

  let outputText = originalOutput;
  for (const [specifier, url] of Object.entries(dependencyUrls)) {
    outputText = outputText.replaceAll(`from '${specifier}'`, `from '${url}'`);
  }
  const encoded = Buffer.from(outputText).toString('base64');
  return import(`data:text/javascript;base64,${encoded}`);
}

async function getTypeScriptModuleUrl(relativePath) {
  const source = await readFile(new URL(relativePath, import.meta.url), 'utf8');
  const { outputText, diagnostics } = ts.transpileModule(source, {
    compilerOptions: {
      module: ts.ModuleKind.ESNext,
      target: ts.ScriptTarget.ES2022,
    },
    reportDiagnostics: true,
  });
  const errors = diagnostics.filter((diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error);
  assert.deepEqual(errors, [], `TypeScript transpilation failed for ${relativePath}`);
  return `data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`;
}

const apiClientUrl = new URL('../src/services/api/apiClient.ts', import.meta.url);
const apiClientSource = await readFile(apiClientUrl, 'utf8');
const apiClientOutput = ts.transpileModule(apiClientSource, {
  compilerOptions: {
    module: ts.ModuleKind.ESNext,
    target: ts.ScriptTarget.ES2022,
  },
}).outputText;
const apiClientModuleUrl = `data:text/javascript;base64,${Buffer.from(apiClientOutput).toString('base64')}`;
const { apiClient, ApiError } = await import(apiClientModuleUrl);
const timeoutMessagesModuleUrl = await getTypeScriptModuleUrl('../src/services/api/authTimeoutMessages.ts');
const { AUTH_TIMEOUT_MESSAGES } = await import(timeoutMessagesModuleUrl);
const { checkBackendReachability } = await importTypeScript('../src/services/api/healthApi.ts', {
  './apiClient': apiClientModuleUrl,
});
const {
  formatAuthApiError,
  formatRegistrationAutoLoginFailure,
  RegistrationAutoLoginError,
  runPostRegistrationLogin,
} = await importTypeScript('../src/services/api/authErrors.ts', {
  './authTimeoutMessages': timeoutMessagesModuleUrl,
});

const originalFetch = globalThis.fetch;
const originalWindow = Object.getOwnPropertyDescriptor(globalThis, 'window');
const originalLocalStorage = Object.getOwnPropertyDescriptor(globalThis, 'localStorage');
const originalCustomEvent = globalThis.CustomEvent;

let scheduledDelays;
let authEvents;
let removedStorageKeys;

function installBrowserGlobals() {
  scheduledDelays = [];
  authEvents = [];
  removedStorageKeys = [];

  Object.defineProperty(globalThis, 'localStorage', {
    configurable: true,
    value: {
      getItem: () => null,
      removeItem: (key) => removedStorageKeys.push(key),
    },
  });
  globalThis.CustomEvent = class CustomEvent {
    constructor(type) {
      this.type = type;
    }
  };
  Object.defineProperty(globalThis, 'window', {
    configurable: true,
    value: {
      setTimeout(callback, delay) {
        scheduledDelays.push(delay);
        return globalThis.setTimeout(callback, Math.min(delay, 5));
      },
      clearTimeout: globalThis.clearTimeout,
      dispatchEvent: (event) => authEvents.push(event.type),
    },
  });
}

function restoreBrowserGlobals() {
  globalThis.fetch = originalFetch;
  globalThis.CustomEvent = originalCustomEvent;
  if (originalWindow) Object.defineProperty(globalThis, 'window', originalWindow);
  else delete globalThis.window;
  if (originalLocalStorage) Object.defineProperty(globalThis, 'localStorage', originalLocalStorage);
  else delete globalThis.localStorage;
}

test('timeout messages describe timeouts without asserting a cold start', () => {
  assert.match(AUTH_TIMEOUT_MESSAGES.login, /timed out/i);
  assert.doesNotMatch(AUTH_TIMEOUT_MESSAGES.login, /wake up|cold start/i);
  assert.match(AUTH_TIMEOUT_MESSAGES.registration, /timed out/i);
  assert.match(AUTH_TIMEOUT_MESSAGES.registrationAutoLogin, /timed out/i);
});

test('unauthenticated health requests omit JWT, cookies, and credentials', async () => {
  installBrowserGlobals();
  let requestUrl;
  let requestConfig;
  let tokenReads = 0;
  globalThis.localStorage.getItem = () => {
    tokenReads += 1;
    return 'must-not-be-read';
  };
  globalThis.fetch = async (url, config) => {
    requestUrl = url;
    requestConfig = config;
    return new Response(JSON.stringify({ status: 'UP' }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    });
  };

  try {
    await checkBackendReachability();
    assert.match(requestUrl, /\/api\/health$/);
    assert.equal(requestConfig.credentials, 'omit');
    assert.equal(requestConfig.body, undefined);
    assert.equal(requestConfig.cache, 'no-store');
    assert.equal(requestConfig.headers.Authorization, undefined);
    assert.equal(requestConfig.headers.Cookie, undefined);
    assert.equal(requestConfig.headers['Content-Type'], undefined);
    assert.equal(tokenReads, 0);
  } finally {
    restoreBrowserGlobals();
  }
});

test('api client maps an aborted request to a distinct timeout error', async () => {
  installBrowserGlobals();
  globalThis.fetch = (_url, { signal }) => new Promise((_resolve, reject) => {
    signal.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')), { once: true });
  });

  try {
    await assert.rejects(
      apiClient('/api/auth/login', {
        method: 'POST',
        timeoutMs: 75_000,
        timeoutMessage: AUTH_TIMEOUT_MESSAGES.login,
      }),
      (error) => error instanceof ApiError
        && error.status === 408
        && error.data.message === AUTH_TIMEOUT_MESSAGES.login,
    );
    assert.deepEqual(scheduledDelays, [75_000]);
  } finally {
    restoreBrowserGlobals();
  }
});

test('api timeout remains active until the response body has been consumed', async () => {
  installBrowserGlobals();
  globalThis.fetch = async (_url, { signal }) => ({
    ok: true,
    status: 200,
    json: () => new Promise((_resolve, reject) => {
      signal.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')), { once: true });
    }),
  });

  try {
    await assert.rejects(
      apiClient('/api/auth/login', {
        method: 'POST',
        timeoutMs: 5,
        timeoutMessage: AUTH_TIMEOUT_MESSAGES.login,
      }),
      (error) => error instanceof ApiError
        && error.status === 408
        && error.data.message === AUTH_TIMEOUT_MESSAGES.login,
    );
  } finally {
    restoreBrowserGlobals();
  }
});

test('a stalled HTTP error body is reported as a timeout, not the original status', async () => {
  installBrowserGlobals();
  globalThis.fetch = async (_url, { signal }) => ({
    ok: false,
    status: 503,
    statusText: 'Service Unavailable',
    json: () => new Promise((_resolve, reject) => {
      signal.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')), { once: true });
    }),
  });

  try {
    await assert.rejects(
      apiClient('/api/auth/login', { method: 'POST', timeoutMs: 5 }),
      (error) => error instanceof ApiError && error.status === 408,
    );
  } finally {
    restoreBrowserGlobals();
  }
});

test('api client keeps network failures distinct from HTTP failures', async () => {
  installBrowserGlobals();
  globalThis.fetch = async () => { throw new TypeError('network unavailable'); };

  try {
    await assert.rejects(apiClient('/api/auth/login'), (error) =>
      error instanceof ApiError
      && error.status === 0
      && error.data.error === 'Network error');

    globalThis.fetch = async () => new Response(JSON.stringify({
      error: 'Unauthorized', message: 'Invalid email or password',
    }), { status: 401, headers: { 'Content-Type': 'application/json' } });

    await assert.rejects(apiClient('/api/auth/login'), (error) =>
      error instanceof ApiError
      && error.status === 401
      && error.data.message === 'Invalid email or password');
    assert.deepEqual(authEvents, ['auth:unauthorized']);
    assert.deepEqual(removedStorageKeys, ['kodbtw_jwt']);

    globalThis.fetch = async () => new Response(JSON.stringify({
      error: 'Bad Gateway', message: 'The platform is temporarily unavailable.',
    }), { status: 502, headers: { 'Content-Type': 'application/json' } });

    await assert.rejects(apiClient('/api/auth/login'), (error) =>
      error instanceof ApiError
      && error.status === 502
      && error.data.message === 'The platform is temporarily unavailable.');
  } finally {
    restoreBrowserGlobals();
  }
});

test('post-registration login failure is marked as partial success with its safe reason', async () => {
  const loginError = new ApiError(401, {
    error: 'Unauthorized',
    message: 'Invalid email or password',
  });

  await assert.rejects(
    runPostRegistrationLogin(async () => { throw loginError; }),
    (error) => error instanceof RegistrationAutoLoginError
      && error.originalError === loginError,
  );

  const message = formatRegistrationAutoLoginFailure();
  assert.match(message, /Your account was created/i);
  assert.match(message, /sign in from the login page/i);
  assert.doesNotMatch(message, /Invalid email or password/);
  assert.match(formatRegistrationAutoLoginFailure(), /Your account was created/i);
});

test('authentication error messages are safe and preserve useful error categories', () => {
  assert.match(formatAuthApiError(401, 'login'), /invalid email or password/i);
  assert.match(formatAuthApiError(0, 'login'), /unable to reach/i);
  assert.match(formatAuthApiError(408, 'login'), /timed out/i);
  assert.match(formatAuthApiError(409, 'registration'), /already exists/i);
  assert.match(formatAuthApiError(503, 'registration'), /try again later/i);
  assert.doesNotMatch(formatAuthApiError(503, 'registration'), /database|secret|stack|provider/i);
});
