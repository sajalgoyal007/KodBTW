import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/services/api/apiClient.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);
const { ApiError } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

test('API errors expose safe status-based messages instead of backend exception text', () => {
  const sensitiveDetails = 'SQL connection string, bearer token, and stack trace';
  const error = new ApiError(500, { error: 'Internal Server Error', message: sensitiveDetails });

  assert.equal(error.message, 'KodBTW could not complete the request. Please try again later.');
  assert.doesNotMatch(error.message, /SQL|bearer|stack trace/i);
  assert.equal(error.data.message, sensitiveDetails);
});

test('API error messages preserve safe timeout, network, and rate-limit guidance', () => {
  assert.match(new ApiError(408, {}).message, /timed out/i);
  assert.equal(new ApiError(408, {}, 'The connection check timed out. Retry when ready.').message,
    'The connection check timed out. Retry when ready.');
  assert.match(new ApiError(0, {}).message, /connection/i);
  assert.match(new ApiError(429, {}).message, /too many requests/i);
});
