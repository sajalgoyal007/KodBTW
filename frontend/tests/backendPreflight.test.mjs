import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

async function importTypeScript(relativePath) {
  const source = await readFile(new URL(relativePath, import.meta.url), 'utf8');
  const { outputText, diagnostics } = ts.transpileModule(source, {
    compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
    reportDiagnostics: true,
  });
  const errors = diagnostics.filter((diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error);
  assert.deepEqual(errors, [], `TypeScript transpilation failed for ${relativePath}`);
  return import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);
}

const { createBackendPreflight } = await importTypeScript('../src/services/api/backendPreflight.ts');

test('starts pending and becomes reachable without submitting credentials', async () => {
  let healthCalls = 0;
  let authCalls = 0;
  const preflight = createBackendPreflight(async () => { healthCalls += 1; });

  assert.equal(preflight.getStatus(), 'checking');
  assert.equal(preflight.canSubmit(), false);
  assert.equal(await preflight.start(), true);
  assert.equal(preflight.getStatus(), 'reachable');
  assert.equal(healthCalls, 1);
  assert.equal(authCalls, 0);
  assert.equal(preflight.canSubmit(), true);
  assert.equal(await preflight.submitIfReady(async () => { authCalls += 1; }), true);
  assert.equal(authCalls, 1);
});

test('failed check blocks auth and a manual retry can recover', async () => {
  let calls = 0;
  const preflight = createBackendPreflight(async () => {
    calls += 1;
    if (calls === 1) throw new Error('unavailable');
  });

  assert.equal(await preflight.check(), false);
  assert.equal(preflight.getStatus(), 'failed');
  assert.equal(preflight.canSubmit(), false);
  let authCalls = 0;
  assert.equal(await preflight.submitIfReady(async () => { authCalls += 1; }), false);
  assert.equal(authCalls, 0);

  assert.equal(await preflight.check(), true);
  assert.equal(preflight.getStatus(), 'reachable');
  assert.equal(preflight.canSubmit(), true);
  assert.equal(calls, 2);
});

test('automatic startup check runs once; another attempt requires manual retry', async () => {
  let calls = 0;
  const preflight = createBackendPreflight(async () => {
    calls += 1;
    if (calls === 1) throw new Error('offline');
  });

  assert.equal(await preflight.start(), false);
  assert.equal(await preflight.start(), false);
  assert.equal(calls, 1);

  assert.equal(await preflight.check(), true);
  assert.equal(calls, 2);
});

test('concurrent checks share one in-flight health request', async () => {
  let resolveHealth;
  let calls = 0;
  const preflight = createBackendPreflight(() => {
    calls += 1;
    return new Promise((resolve) => { resolveHealth = resolve; });
  });

  const first = preflight.check();
  const second = preflight.check();
  assert.equal(first, second);
  assert.equal(calls, 0); // Health callback starts on the next microtask.
  await Promise.resolve();
  assert.equal(calls, 1);

  resolveHealth();
  assert.equal(await first, true);
  assert.equal(preflight.getStatus(), 'reachable');
});

test('pending auth submission cannot invoke login or registration callbacks', async () => {
  let authCalls = 0;
  const preflight = createBackendPreflight(async () => undefined);

  assert.equal(await preflight.submitIfReady(async () => { authCalls += 1; }), false);
  assert.equal(authCalls, 0);
});

test('failed preflight blocks login or registration callbacks until manual retry succeeds', async () => {
  let healthCalls = 0;
  let authCalls = 0;
  const preflight = createBackendPreflight(async () => {
    healthCalls += 1;
    if (healthCalls === 1) throw new Error('unavailable');
  });

  assert.equal(await preflight.start(), false);
  assert.equal(await preflight.submitIfReady(async () => { authCalls += 1; }), false);
  assert.equal(authCalls, 0);
  assert.equal(await preflight.check(), true);
  assert.equal(await preflight.submitIfReady(async () => { authCalls += 1; }), true);
  assert.equal(authCalls, 1);
});

test('expired reachability triggers one health recheck and requires a separate auth submit', async () => {
  let now = 1_000;
  let calls = 0;
  let resolveRefresh;
  let authCalls = 0;
  const preflight = createBackendPreflight(() => {
    calls += 1;
    if (calls === 2) return new Promise((resolve) => { resolveRefresh = resolve; });
    return Promise.resolve();
  }, { now: () => now, cacheTtlMs: 100 });

  assert.equal(await preflight.start(), true);
  assert.equal(preflight.canSubmit(), true);
  now += 101;
  assert.equal(preflight.canSubmit(), false);
  assert.equal(await preflight.submitIfReady(async () => { authCalls += 1; }), false);
  assert.equal(authCalls, 0);
  await Promise.resolve();
  assert.equal(calls, 2);
  assert.equal(preflight.getStatus(), 'checking');

  resolveRefresh();
  await Promise.resolve();
  await Promise.resolve();
  assert.equal(preflight.getStatus(), 'reachable');
  assert.equal(authCalls, 0);
  assert.equal(await preflight.submitIfReady(async () => { authCalls += 1; }), true);
  assert.equal(authCalls, 1);
});

test('expired reachability becomes stale and requires an explicit retry before auth', async () => {
  let now = 10_000;
  let healthCalls = 0;
  let authCalls = 0;
  const preflight = createBackendPreflight(async () => { healthCalls += 1; }, {
    now: () => now,
    cacheTtlMs: 100,
  });

  await preflight.start();
  now += 100;
  preflight.expireIfStale();

  assert.equal(preflight.getStatus(), 'stale');
  assert.equal(preflight.canSubmit(), false);
  assert.equal(await preflight.submitIfReady(async () => { authCalls += 1; }), false);
  assert.equal(authCalls, 0);
  assert.equal(healthCalls, 1);

  assert.equal(await preflight.check(), true);
  assert.equal(preflight.getStatus(), 'reachable');
  assert.equal(await preflight.submitIfReady(async () => { authCalls += 1; }), true);
  assert.equal(authCalls, 1);
  assert.equal(healthCalls, 2);
});

test('unsubscribed components do not receive later preflight state updates', async () => {
  let updates = 0;
  const preflight = createBackendPreflight(async () => undefined);
  const unsubscribe = preflight.subscribe(() => { updates += 1; });

  unsubscribe();
  await preflight.start();

  assert.equal(updates, 0);
});
