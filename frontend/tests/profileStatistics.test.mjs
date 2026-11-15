import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/utils/profileStatistics.ts', import.meta.url), 'utf8');
const compiled = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(compiled.diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);
const { getProfileMetricValue } = await import(`data:text/javascript;base64,${Buffer.from(compiled.outputText).toString('base64')}`);

test('missing aggregate data is not shown as zero when no profiles are connected', () => {
  assert.equal(getProfileMetricValue(0, 0), null);
  assert.equal(getProfileMetricValue(0, undefined), null);
  assert.equal(getProfileMetricValue(null, 2), null);
});

test('a genuine zero remains visible when a profile is connected', () => {
  assert.equal(getProfileMetricValue(0, 1), 0);
  assert.equal(getProfileMetricValue(12, 1), 12);
});
