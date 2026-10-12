import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/utils/platformStats.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(
  diagnostics.filter((diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error),
  [],
);
const { formatPlatformSharePercentage } = await import(
  `data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`,
);

test('unknown share percentages stay unavailable while numeric zero remains zero', () => {
  assert.equal(formatPlatformSharePercentage(null), '—');
  assert.equal(formatPlatformSharePercentage(undefined), '—');
  assert.equal(formatPlatformSharePercentage(Number.NaN), '—');
  assert.equal(formatPlatformSharePercentage(0), '0.0%');
  assert.equal(formatPlatformSharePercentage(25.36), '25.4%');
});
