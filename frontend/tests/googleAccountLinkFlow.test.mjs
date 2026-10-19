import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/services/api/googleAccountLinkFlow.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);
const moduleUrl = `data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`;
const { runGoogleAccountLinkFlow } = await import(moduleUrl);
const connectionStateSource = await readFile(new URL('../src/components/profile/googleAccountConnectionState.ts', import.meta.url), 'utf8');
const connectionStateModule = ts.transpileModule(connectionStateSource, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(connectionStateModule.diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);
const { getGoogleAccountConnectionView } = await import(`data:text/javascript;base64,${Buffer.from(connectionStateModule.outputText).toString('base64')}`);

test('successful account linking updates connection state and clears the password form', async () => {
  const calls = { link: [], linking: [], errors: [], connected: [], clear: 0 };
  await runGoogleAccountLinkFlow({
    credential: 'fixture-credential', challengeId: 'fixture-challenge', inProgress: { current: false },
    linkIdentity: async (...args) => calls.link.push(args),
    setIsLinking: (value) => calls.linking.push(value), setError: (value) => calls.errors.push(value),
    setConnected: (value) => calls.connected.push(value), clearSensitiveFormState: () => calls.clear++,
    formatError: () => 'safe error',
  });
  assert.deepEqual(calls.link, [['fixture-credential', 'fixture-challenge']]);
  assert.deepEqual(calls.connected, [true]);
  assert.equal(calls.clear, 1);
  assert.deepEqual(calls.errors, [null]);
  assert.deepEqual(calls.linking, [true, false]);
});

test('failed linking shows only the safe mapped message and leaves connection false', async () => {
  const calls = { errors: [], connected: [], clear: 0 };
  await runGoogleAccountLinkFlow({
    credential: 'fixture-credential', challengeId: 'fixture-challenge', inProgress: { current: false },
    linkIdentity: async () => { throw new Error('sensitive database detail'); },
    setIsLinking: () => {}, setError: (value) => calls.errors.push(value),
    setConnected: (value) => calls.connected.push(value), clearSensitiveFormState: () => calls.clear++,
    formatError: () => 'This Google account could not be connected. Try again.',
  });
  assert.deepEqual(calls.errors, [null, 'This Google account could not be connected. Try again.']);
  assert.deepEqual(calls.connected, []);
  assert.equal(calls.clear, 0);
  assert.doesNotMatch(calls.errors.join(' '), /sensitive database detail/);
});

test('duplicate in-flight link callbacks submit only once', async () => {
  let finish;
  let submits = 0;
  const inProgress = { current: false };
  const options = {
    credential: 'fixture-credential', challengeId: 'fixture-challenge', inProgress,
    linkIdentity: () => { submits += 1; return new Promise((resolve) => { finish = resolve; }); },
    setIsLinking: () => {}, setError: () => {}, setConnected: () => {}, clearSensitiveFormState: () => {},
    formatError: () => 'safe error',
  };
  const first = runGoogleAccountLinkFlow(options);
  await Promise.resolve();
  const duplicate = runGoogleAccountLinkFlow(options);
  await duplicate;
  assert.equal(submits, 1);
  assert.equal(inProgress.current, true);
  finish();
  await first;
  assert.equal(inProgress.current, false);
});

test('connection status distinguishes loading, error, connected and not-connected states', () => {
  assert.equal(getGoogleAccountConnectionView(true, null, null), 'loading');
  assert.equal(getGoogleAccountConnectionView(false, 'status unavailable', null), 'error');
  assert.equal(getGoogleAccountConnectionView(false, null, true), 'connected');
  assert.equal(getGoogleAccountConnectionView(false, null, false), 'disconnected');
});

test('Profile renders Account Connections below the summary and above competitive stats', async () => {
  const profileSource = await readFile(new URL('../src/components/profile/GoogleAccountConnection.tsx', import.meta.url), 'utf8');
  const pageSource = await readFile(new URL('../src/pages/ProfilePage.tsx', import.meta.url), 'utf8');
  assert.match(pageSource, /<GoogleAccountConnection\s*\/>/);
  const summaryPosition = pageSource.indexOf('/* Completed Profile Display */');
  const connectionsPosition = pageSource.indexOf('<GoogleAccountConnection />');
  const statsPosition = pageSource.indexOf('/* Coding Stats Highlight section */');
  assert.ok(summaryPosition >= 0 && summaryPosition < connectionsPosition && connectionsPosition < statsPosition);
  assert.match(profileSource, />Account Connections</);
  assert.match(profileSource, /'Not connected'/);
  assert.match(profileSource, /Connect Google Account/);
  assert.match(profileSource, /authApi\.getGoogleIdentityStatus/);
  assert.match(profileSource, /authApi\.createGoogleLinkChallenge\(currentPassword\)/);
  assert.match(profileSource, /authApi\.linkGoogleIdentity/);
  assert.match(profileSource, /<GoogleSignInButton\b/);
  assert.match(profileSource, /autoComplete="current-password"/);
  assert.match(profileSource, /role="alert"/);
});
