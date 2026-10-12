import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/utils/requestCoordination.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);
const { createLatestRequestTracker, createSingleFlightGate } = await import(
  `data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`,
);

test('single-flight gate rejects overlapping refreshes until the active one finishes', () => {
  const gate = createSingleFlightGate();
  assert.equal(gate.tryEnter(), true);
  assert.equal(gate.tryEnter(), false);
  gate.leave();
  assert.equal(gate.tryEnter(), true);
  gate.leave();
});

test('latest request tracker prevents older responses and unmounted requests from committing', () => {
  const tracker = createLatestRequestTracker();
  const older = tracker.begin();
  const latest = tracker.begin();

  assert.equal(tracker.isCurrent(older), false);
  assert.equal(tracker.isCurrent(latest), true);

  tracker.invalidate();
  assert.equal(tracker.isCurrent(latest), false);
});
