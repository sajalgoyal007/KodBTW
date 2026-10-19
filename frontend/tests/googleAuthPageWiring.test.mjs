import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const loginSource = await readFile(new URL('../src/pages/LoginPage.tsx', import.meta.url), 'utf8');
const registrationSource = await readFile(new URL('../src/pages/RegisterPage.tsx', import.meta.url), 'utf8');
const googleButtonSource = await readFile(new URL('../src/components/auth/GoogleSignInButton.tsx', import.meta.url), 'utf8');
const flowSource = await readFile(new URL('../src/services/api/googleAuthPageFlow.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(flowSource, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);
const flowUrl = `data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`;
const { runGoogleAuthPageFlow } = await import(flowUrl);

for (const [pageName, source] of [
  ['login', loginSource],
  ['registration', registrationSource],
]) {
  test(`${pageName} renders the reusable Google button and connects its credential callback`, () => {
    assert.match(source, /import\s*\{\s*GoogleSignInButton\s*\}\s*from\s*['"]\.\.\/components\/auth\/GoogleSignInButton['"]/);
    assert.match(source, /<GoogleSignInButton\b/);
    assert.match(source, /onCredential=\{handleGoogleCredential\}/);
    assert.match(source, /onError=\{handleGoogleError\}/);
    assert.match(source, /runGoogleAuthPageFlow\(/);
    assert.match(source, /loginWithGoogle,/);
  });
}

test('the Google button obtains a server challenge and returns its ID with the credential', () => {
  assert.match(googleButtonSource, /authApi\.createGoogleChallenge\(\)/);
  assert.match(googleButtonSource, /nonce:\s*challenge\.nonce/);
  assert.match(googleButtonSource, /onCredential\(response\.credential,\s*challenge\.challengeId\)/);
});

for (const [pageName, destination] of [['login', '/dashboard'], ['registration', '/dashboard']]) {
  test(`${pageName} flow forwards the challenge to authentication and navigates only after success`, async () => {
    const challenge = {
      challengeId: `server-challenge-${pageName}`,
      nonce: 'server-issued-nonce',
      expiresAt: '2026-01-01T00:05:00Z',
    };
    const calls = { challenge: 0, auth: [], navigation: [], loading: [], errors: [] };
    const inProgress = { current: false };
    const issueChallenge = async () => {
      calls.challenge += 1;
      return challenge;
    };
    const challengeForSignIn = await issueChallenge();

    await runGoogleAuthPageFlow({
      credential: `fixture-google-credential-${pageName}`,
      challengeId: challengeForSignIn.challengeId,
      destination,
      inProgress,
      submitIfReady: async (submit) => { await submit(); return true; },
      loginWithGoogle: async (credential, challengeId) => calls.auth.push({ credential, challengeId }),
      navigate: (...args) => calls.navigation.push(args),
      setError: (message) => calls.errors.push(message),
      setIsLoading: (value) => calls.loading.push(value),
      formatError: () => 'Google sign-in could not be completed. Please try again.',
    });

    assert.equal(calls.challenge, 1);
    assert.deepEqual(calls.auth, [{
      credential: `fixture-google-credential-${pageName}`,
      challengeId: challenge.challengeId,
    }]);
    assert.deepEqual(calls.navigation, [[destination, { replace: true }]]);
    assert.deepEqual(calls.loading, [true, false]);
    assert.deepEqual(calls.errors, [null]);
    assert.equal(inProgress.current, false);
  });
}

test('authentication failure shows the safe mapped message and does not navigate', async () => {
  const calls = { navigation: [], errors: [], loading: [] };
  await runGoogleAuthPageFlow({
    credential: 'fixture-google-credential',
    challengeId: 'fixture-challenge',
    destination: '/dashboard',
    inProgress: { current: false },
    submitIfReady: async (submit) => { await submit(); return true; },
    loginWithGoogle: async () => { throw new Error('sensitive backend detail'); },
    navigate: (...args) => calls.navigation.push(args),
    setError: (message) => calls.errors.push(message),
    setIsLoading: (value) => calls.loading.push(value),
    formatError: () => 'Google sign-in could not be completed. Please try again.',
  });

  assert.deepEqual(calls.navigation, []);
  assert.deepEqual(calls.errors, [null, 'Google sign-in could not be completed. Please try again.']);
  assert.doesNotMatch(calls.errors.join(' '), /sensitive backend detail/);
  assert.deepEqual(calls.loading, [true, false]);
});

test('an in-flight Google authentication ignores duplicate credential submissions', async () => {
  let releaseLogin;
  let authCalls = 0;
  const inProgress = { current: false };
  const options = {
    credential: 'fixture-google-credential',
    challengeId: 'fixture-challenge',
    destination: '/dashboard',
    inProgress,
    submitIfReady: async (submit) => { await submit(); return true; },
    loginWithGoogle: () => {
      authCalls += 1;
      return new Promise((resolve) => { releaseLogin = resolve; });
    },
    navigate: () => {},
    setError: () => {},
    setIsLoading: () => {},
    formatError: () => 'Google sign-in could not be completed. Please try again.',
  };

  const firstAttempt = runGoogleAuthPageFlow(options);
  await Promise.resolve();
  await Promise.resolve();
  const duplicateAttempt = runGoogleAuthPageFlow(options);
  await duplicateAttempt;
  assert.equal(authCalls, 1);
  assert.equal(inProgress.current, true);

  releaseLogin();
  await firstAttempt;
  assert.equal(inProgress.current, false);
});

test('email and password forms and their submit controls remain available on both pages', () => {
  for (const source of [loginSource, registrationSource]) {
    assert.match(source, /<form\s+onSubmit=\{handleSubmit\}>/);
    assert.match(source, /<input[\s\S]*?type="email"/);
    assert.match(source, /<input[\s\S]*?type="password"/);
    assert.match(source, /<button[\s\S]*?type="submit"[\s\S]*?disabled=\{isLoading\s*\|\|\s*isSessionLoading/);
    assert.match(source, /const validate = \(\): boolean/);
  }
  assert.match(loginSource, />\s*Sign In\s*</);
  assert.match(registrationSource, />\s*Create Account\s*</);
});
