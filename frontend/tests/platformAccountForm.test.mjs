import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/utils/platformAccountForm.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(
  diagnostics.filter((diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error),
  [],
);
const { runSingleSubmission, validatePlatformAccountForm } = await import(
  `data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`,
);

test('profile form validation trims usernames and accepts only valid HTTP(S) profile URLs', () => {
  assert.deepEqual(validatePlatformAccountForm({ username: '   ', profileUrl: '' }), {
    username: 'Username is required.',
  });
  assert.deepEqual(validatePlatformAccountForm({ username: ` ${'x'.repeat(101)} `, profileUrl: '' }), {
    username: 'Username must not exceed 100 characters.',
  });
  assert.deepEqual(validatePlatformAccountForm({ username: '  handle  ', profileUrl: 'HTTPS://example.com/u/handle' }), {});
  assert.deepEqual(validatePlatformAccountForm({ username: 'handle', profileUrl: 'javascript:alert(1)' }), {
    profileUrl: 'Enter a valid HTTP or HTTPS profile URL.',
  });
  assert.deepEqual(validatePlatformAccountForm({ username: 'handle', profileUrl: 'https://' }), {
    profileUrl: 'Enter a valid HTTP or HTTPS profile URL.',
  });
});

test('concurrent account submissions are deduplicated and the guard releases after completion', async () => {
  const lock = { current: false };
  let resolveSubmission;
  let calls = 0;
  const pending = runSingleSubmission(lock, () => {
    calls += 1;
    return new Promise((resolve) => { resolveSubmission = resolve; });
  });

  assert.equal(await runSingleSubmission(lock, async () => { calls += 1; }), false);
  assert.equal(calls, 1);
  resolveSubmission();
  assert.equal(await pending, true);
  assert.equal(lock.current, false);
  assert.equal(await runSingleSubmission(lock, async () => { calls += 1; }), true);
  assert.equal(calls, 2);
  await assert.rejects(runSingleSubmission(lock, async () => { throw new Error('expected'); }));
  assert.equal(lock.current, false);
});
