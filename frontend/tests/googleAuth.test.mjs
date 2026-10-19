import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

async function moduleUrl(relativePath, dependencyUrls = {}) {
  const source = await readFile(new URL(relativePath, import.meta.url), 'utf8');
  const { outputText, diagnostics } = ts.transpileModule(source, {
    compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
    reportDiagnostics: true,
  });
  assert.deepEqual(diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);
  let rewritten = outputText;
  for (const [specifier, url] of Object.entries(dependencyUrls)) rewritten = rewritten.replaceAll(`from '${specifier}'`, `from '${url}'`);
  return `data:text/javascript;base64,${Buffer.from(rewritten).toString('base64')}`;
}

let request;
const apiClientUrl = `data:text/javascript;base64,${Buffer.from(
  'export async function apiClient(...args) { globalThis.googleAuthRequest = args; return args[0].endsWith("/challenge") ? { challengeId: "challenge-id", nonce: "server-issued-nonce", expiresAt: "2026-01-01T00:05:00Z" } : { token: "kodbtw-token", user: { id: 1, name: "Test", email: "test@example.com" } }; }',
).toString('base64')}`;
const timeoutUrl = await moduleUrl('../src/services/api/authTimeoutMessages.ts');
const authApiUrl = await moduleUrl('../src/services/api/authApi.ts', {
  './apiClient': apiClientUrl,
  './authTimeoutMessages': timeoutUrl,
});
const { authApi } = await import(authApiUrl);
const authErrorsUrl = await moduleUrl('../src/services/api/authErrors.ts', {
  './authTimeoutMessages': timeoutUrl,
});
const { formatGoogleAuthError, formatGoogleLinkError } = await import(authErrorsUrl);

test('Google ID credential is posted without stored KodBTW authentication', async () => {
  delete globalThis.googleAuthRequest;
  const challenge = await authApi.createGoogleChallenge();
  request = globalThis.googleAuthRequest;
  assert.equal(request[0], '/api/auth/google/challenge');
  assert.equal(request[1].method, 'POST');
  assert.equal(request[1].auth, false);
  assert.equal(challenge.nonce, 'server-issued-nonce');

  const result = await authApi.googleLogin({ credential: 'google-id-token', challengeId: challenge.challengeId });
  request = globalThis.googleAuthRequest;
  assert.equal(request[0], '/api/auth/google');
  assert.equal(request[1].method, 'POST');
  assert.equal(request[1].auth, false);
  assert.equal(request[1].timeoutMs, 75_000);
  assert.deepEqual(JSON.parse(request[1].body), { credential: 'google-id-token', challengeId: 'challenge-id' });
  assert.match(request[1].headers['X-Request-ID'], /^[0-9a-f-]{36}$/i);
  assert.equal(result.token, 'kodbtw-token');
});

test('Google auth failures map only known safe messages', () => {
  assert.match(formatGoogleAuthError(409, 'GOOGLE_ACCOUNT_REQUIRES_EXISTING_SIGN_IN'), /sign in with your existing method/i);
  assert.match(formatGoogleAuthError(503, 'GOOGLE_AUTH_NOT_CONFIGURED'), /not configured on the server/i);
  assert.match(formatGoogleAuthError(401, 'INVALID_GOOGLE_CREDENTIAL'), /could not be verified/i);
  assert.match(formatGoogleAuthError(503, 'GOOGLE_VERIFICATION_UNAVAILABLE'), /try again/i);
  assert.doesNotMatch(formatGoogleAuthError(500, 'UNEXPECTED'), /raw backend|stack trace|internal/i);
});

test('Google account linking uses authenticated endpoints and never sends a target user id', async () => {
  await authApi.createGoogleLinkChallenge('current-password-fixture');
  request = globalThis.googleAuthRequest;
  assert.equal(request[0], '/api/auth/google/link/challenge');
  assert.equal(request[1].method, 'POST');
  assert.notEqual(request[1].auth, false);
  assert.deepEqual(JSON.parse(request[1].body), { currentPassword: 'current-password-fixture' });

  await authApi.linkGoogleIdentity({ credential: 'fixture-google-credential', challengeId: 'user-bound-challenge' });
  request = globalThis.googleAuthRequest;
  assert.equal(request[0], '/api/auth/google/link');
  assert.equal(request[1].method, 'POST');
  assert.notEqual(request[1].auth, false);
  assert.deepEqual(JSON.parse(request[1].body), {
    credential: 'fixture-google-credential', challengeId: 'user-bound-challenge',
  });
  assert.equal(Object.hasOwn(JSON.parse(request[1].body), 'userId'), false);

  await authApi.getGoogleIdentityStatus();
  request = globalThis.googleAuthRequest;
  assert.equal(request[0], '/api/auth/google/identity');
  assert.equal(request[1].method, 'GET');
  assert.notEqual(request[1].auth, false);
});

test('Google link password and identity conflict errors are safe and actionable', () => {
  assert.match(formatGoogleLinkError(403, 'GOOGLE_LINK_REAUTHENTICATION_REQUIRED'), /password could not be verified/i);
  assert.match(formatGoogleLinkError(409, 'GOOGLE_IDENTITY_CONFLICT'), /already connected/i);
  assert.doesNotMatch(formatGoogleLinkError(500, 'UNEXPECTED'), /raw backend|stack trace|internal/i);
});
