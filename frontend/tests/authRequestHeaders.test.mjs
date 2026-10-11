import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

async function importTypeScript(relativePath, dependencyUrls = {}) {
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

  let resolvedOutput = outputText;
  for (const [specifier, url] of Object.entries(dependencyUrls)) {
    resolvedOutput = resolvedOutput.replaceAll(`from '${specifier}'`, `from '${url}'`);
  }
  return import(`data:text/javascript;base64,${Buffer.from(resolvedOutput).toString('base64')}`);
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

const apiClientSource = await readFile(new URL('../src/services/api/apiClient.ts', import.meta.url), 'utf8');
const { outputText: apiClientOutput } = ts.transpileModule(apiClientSource, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
});
const apiClientModuleUrl = `data:text/javascript;base64,${Buffer.from(apiClientOutput).toString('base64')}`;
const timeoutMessagesModuleUrl = await getTypeScriptModuleUrl('../src/services/api/authTimeoutMessages.ts');
const { authApi } = await importTypeScript('../src/services/api/authApi.ts', {
  './apiClient': apiClientModuleUrl,
  './authTimeoutMessages': timeoutMessagesModuleUrl,
});

const originalFetch = globalThis.fetch;
const originalWindow = Object.getOwnPropertyDescriptor(globalThis, 'window');
const originalLocalStorage = Object.getOwnPropertyDescriptor(globalThis, 'localStorage');

test('login and registration omit stored JWTs and send correlation IDs', async () => {
  const requests = [];
  const scheduledTimeouts = [];
  let tokenReads = 0;
  Object.defineProperty(globalThis, 'localStorage', {
    configurable: true,
    value: {
      getItem: () => {
        tokenReads += 1;
        return 'stored-jwt-must-not-be-used';
      },
      removeItem() {},
    },
  });
  Object.defineProperty(globalThis, 'window', {
    configurable: true,
    value: {
      setTimeout(_callback, delay) {
        scheduledTimeouts.push(delay);
        return 1;
      },
      clearTimeout() {},
      dispatchEvent() {},
    },
  });
  globalThis.fetch = async (url, config) => {
    requests.push({ url, config });
    const isLogin = String(url).endsWith('/login');
    return new Response(JSON.stringify(isLogin
      ? { token: 'response-token', user: { id: 1, name: 'Test', email: 'test@example.com' } }
      : { id: 1, name: 'Test', email: 'test@example.com' }), {
      status: isLogin ? 200 : 201,
      headers: { 'Content-Type': 'application/json' },
    });
  };

  try {
    await authApi.login({ email: 'test@example.com', password: 'synthetic-test-password' });
    await authApi.register({ name: 'Test', email: 'test@example.com', password: 'synthetic-test-password' });

    assert.equal(requests.length, 2);
    for (const { config } of requests) {
      assert.equal(config.credentials, 'omit');
      assert.equal(config.headers.Authorization, undefined);
      assert.equal(config.headers['Content-Type'], 'application/json');
      assert.match(config.headers['X-Request-ID'], /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i);
      assert.equal(config.signal.aborted, false);
    }
    assert.equal(tokenReads, 0);
    assert.deepEqual(scheduledTimeouts, [75_000, 75_000]);
  } finally {
    globalThis.fetch = originalFetch;
    if (originalWindow) Object.defineProperty(globalThis, 'window', originalWindow);
    else delete globalThis.window;
    if (originalLocalStorage) Object.defineProperty(globalThis, 'localStorage', originalLocalStorage);
    else delete globalThis.localStorage;
  }
});
