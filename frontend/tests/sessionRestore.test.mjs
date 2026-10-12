import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/services/api/sessionRestore.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(
  diagnostics.filter((diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error),
  [],
  'TypeScript transpilation failed for sessionRestore.ts',
);
const { restoreSession } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

const apiClientSource = await readFile(new URL('../src/services/api/apiClient.ts', import.meta.url), 'utf8');
const { outputText: apiClientOutput, diagnostics: apiClientDiagnostics } = ts.transpileModule(apiClientSource, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(
  apiClientDiagnostics.filter((diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error),
  [],
  'TypeScript transpilation failed for apiClient.ts',
);
const { apiClient, ApiError } = await import(`data:text/javascript;base64,${Buffer.from(apiClientOutput).toString('base64')}`);

const originalFetch = globalThis.fetch;
const originalWindow = Object.getOwnPropertyDescriptor(globalThis, 'window');
const originalLocalStorage = Object.getOwnPropertyDescriptor(globalThis, 'localStorage');
const originalCustomEvent = globalThis.CustomEvent;

function restoreGlobals() {
  globalThis.fetch = originalFetch;
  globalThis.CustomEvent = originalCustomEvent;
  if (originalWindow) Object.defineProperty(globalThis, 'window', originalWindow);
  else delete globalThis.window;
  if (originalLocalStorage) Object.defineProperty(globalThis, 'localStorage', originalLocalStorage);
  else delete globalThis.localStorage;
}

const user = { id: 7, name: 'Local Test', email: 'local@example.test' };

test('no stored token remains anonymous without making a validation request', async () => {
  let requests = 0;
  let clears = 0;

  const result = await restoreSession(null, async () => {
    requests += 1;
    return user;
  }, () => { clears += 1; });

  assert.deepEqual(result, { status: 'anonymous' });
  assert.equal(requests, 0);
  assert.equal(clears, 0);
});

test('a validated saved token restores the user', async () => {
  const result = await restoreSession('opaque-test-token', async () => user, () => {
    assert.fail('A valid session must not clear the saved token');
  });

  assert.deepEqual(result, { status: 'authenticated', user });
});

test('only an explicit 401 clears an invalid saved token', async () => {
  let clears = 0;
  const result = await restoreSession('opaque-test-token', async () => {
    throw Object.assign(new Error('private backend details'), { status: 401 });
  }, () => { clears += 1; });

  assert.deepEqual(result, { status: 'anonymous' });
  assert.equal(clears, 1);
});

test('network, timeout, and server failures preserve the token and report unavailable', async (t) => {
  for (const failure of [
    Object.assign(new Error('network'), { status: 0 }),
    Object.assign(new Error('timeout'), { status: 408 }),
    Object.assign(new Error('server'), { status: 503 }),
  ]) {
    await t.test(`status ${failure.status}`, async () => {
      let clears = 0;
      const result = await restoreSession('opaque-test-token', async () => {
        throw failure;
      }, () => { clears += 1; });

      assert.deepEqual(result, { status: 'unavailable' });
      assert.equal(clears, 0);
    });
  }
});

test('aborted validation is ignored and does not clear the saved token', async () => {
  const controller = new AbortController();
  controller.abort();
  let requests = 0;
  let clears = 0;

  const result = await restoreSession('opaque-test-token', async () => {
    requests += 1;
    return user;
  }, () => { clears += 1; }, controller.signal);

  assert.deepEqual(result, { status: 'cancelled' });
  assert.equal(requests, 0);
  assert.equal(clears, 0);
});

test('a request cancelled during validation is not classified as invalid', async () => {
  const controller = new AbortController();
  let clears = 0;
  const request = restoreSession('opaque-test-token', async (signal) => {
    assert.equal(signal, controller.signal);
    controller.abort();
    throw new DOMException('Aborted', 'AbortError');
  }, () => { clears += 1; }, controller.signal);

  assert.deepEqual(await request, { status: 'cancelled' });
  assert.equal(clears, 0);
});

test('401 from an unauthenticated request does not revoke a saved bearer session', async () => {
  const removals = [];
  const events = [];
  let tokenReads = 0;
  Object.defineProperty(globalThis, 'localStorage', {
    configurable: true,
    value: {
      getItem: () => { tokenReads += 1; return 'saved-session-token'; },
      removeItem: (key) => removals.push(key),
    },
  });
  globalThis.CustomEvent = class CustomEvent {
    constructor(type) { this.type = type; }
  };
  Object.defineProperty(globalThis, 'window', {
    configurable: true,
    value: {
      setTimeout: () => 1,
      clearTimeout() {},
      dispatchEvent: (event) => events.push(event.type),
    },
  });
  globalThis.fetch = async () => new Response(JSON.stringify({
    error: 'Unauthorized', message: 'Invalid email or password',
  }), { status: 401, headers: { 'Content-Type': 'application/json' } });

  try {
    await assert.rejects(
      apiClient('/api/auth/login', { method: 'POST', auth: false }),
      (error) => error instanceof ApiError && error.status === 401,
    );
    assert.equal(tokenReads, 0);
    assert.deepEqual(removals, []);
    assert.deepEqual(events, []);
  } finally {
    restoreGlobals();
  }
});
