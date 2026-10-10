import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

async function importTypeScript(relativePath) {
  const sourceUrl = new URL(relativePath, import.meta.url);
  const source = await readFile(sourceUrl, 'utf8');
  const { outputText, diagnostics } = ts.transpileModule(source, {
    compilerOptions: {
      module: ts.ModuleKind.ESNext,
      target: ts.ScriptTarget.ES2022,
    },
    reportDiagnostics: true,
  });

  const errors = diagnostics.filter((diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error);
  assert.deepEqual(errors, [], `TypeScript transpilation failed for ${relativePath}`);

  const encoded = Buffer.from(outputText).toString('base64');
  return import(`data:text/javascript;base64,${encoded}`);
}

const { apiClient, ApiError } = await importTypeScript('../src/services/api/apiClient.ts');
const { AUTH_TIMEOUT_MESSAGES } = await importTypeScript('../src/services/api/authTimeoutMessages.ts');

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
  assert.match(AUTH_TIMEOUT_MESSAGES.registrationAutoLogin, /account was created/i);
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
