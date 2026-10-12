import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/utils/queuedRefresh.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(diagnostics.filter((diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error), []);
const { createQueuedRefreshRunner } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

test('a refresh requested during a fetch is coalesced into one follow-up fetch', async () => {
  const run = createQueuedRefreshRunner();
  let release;
  const firstTask = new Promise((resolve) => { release = resolve; });
  const calls = [];
  const first = run(async () => { calls.push('initial'); await firstTask; });
  const queued = run(async () => { calls.push('after-sync'); }, true);
  run(async () => { calls.push('also-after-sync'); }, true);

  release();
  await Promise.all([first, queued]);
  assert.deepEqual(calls, ['initial', 'also-after-sync']);
});

test('ordinary concurrent fetch requests share the active fetch without queuing another', async () => {
  const run = createQueuedRefreshRunner();
  let release;
  const activeTask = new Promise((resolve) => { release = resolve; });
  let calls = 0;
  const first = run(async () => { calls += 1; await activeTask; });
  const concurrent = run(async () => { calls += 1; });
  release();
  await Promise.all([first, concurrent]);
  assert.equal(calls, 1);
});
