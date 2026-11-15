import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/config/platformCapabilities.ts', import.meta.url), 'utf8');
const compiled = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(compiled.diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);
const moduleUrl = `data:text/javascript;base64,${Buffer.from(compiled.outputText).toString('base64')}`;
const capabilities = await import(moduleUrl);

test('account-connectable platforms match the backend enum without adding fake connections', async () => {
  const backend = await readFile(new URL('../../backend/src/main/java/com/kodbtw/entity/Platform.java', import.meta.url), 'utf8');
  const enumBody = backend.match(/public enum Platform\s*\{([\s\S]*?)\n\}/)?.[1];
  assert.ok(enumBody);
  const enumValues = [...enumBody.split(';')[0].matchAll(/\b[A-Z][A-Z0-9_]*\b/g)].map((match) => match[0]);
  const accountPlatforms = capabilities.getAccountConnectablePlatforms().map((item) => item.accountPlatform);
  assert.deepEqual([...accountPlatforms].sort(), [...enumValues].sort());

  const syncMethod = backend.match(/hasLiveStatsSource\(\)\s*\{([\s\S]*?)\n\s*\}/)?.[1];
  assert.ok(syncMethod);
  const backendSyncPlatforms = [...syncMethod.matchAll(/this\s*==\s*([A-Z][A-Z0-9_]*)/g)].map((match) => match[1]);
  const modeledSyncPlatforms = capabilities.PLATFORM_CAPABILITIES
    .filter((item) => item.synchronizationEnabled)
    .map((item) => item.id);
  assert.deepEqual([...modeledSyncPlatforms].sort(), [...backendSyncPlatforms].sort());
});

test('GFG and HackerRank permit saved handles but stay source-pending with no live metrics', () => {
  for (const platform of ['GEEKSFORGEEKS', 'HACKERRANK']) {
    const capability = capabilities.getPlatformCapability(platform);
    assert.equal(capability.accountConnectionAvailable, true);
    assert.equal(capability.synchronizationEnabled, false);
    assert.equal(capability.liveStatisticsAvailable, false);
    assert.deepEqual(capability.supportedMetrics, []);
    assert.equal(capability.statisticsSourceTag, null);
    assert.match(capability.availabilityMessage, /live statistics.*unavailable/i);
  }
  assert.equal(capabilities.getPlatformConnectionStatus('HACKERRANK', 'SOURCE_PENDING'), 'Handle saved · live statistics unavailable');
});

test('AtCoder, InterviewBit and CodeStudio have no active account or sync controls', () => {
  for (const platform of ['ATCODER', 'INTERVIEWBIT', 'CODESTUDIO']) {
    const capability = capabilities.getPlatformCapability(platform);
    assert.equal(capability.accountPlatform, null);
    assert.equal(capability.accountConnectionAvailable, false);
    assert.equal(capability.synchronizationEnabled, false);
    assert.equal(capability.profileUrlFor, null);
    assert.equal(capabilities.getPlatformConnectionStatus(platform), 'Unavailable');
  }
});

test('supported metrics and provenance describe current adapters without granting authorization', () => {
  assert.deepEqual(capabilities.getPlatformCapability('CODEFORCES').supportedMetrics, ['rating', 'maxRating']);
  assert.equal(capabilities.getPlatformCapability('CODEFORCES').sourceApprovalStatus, 'documented-public-api');
  assert.equal(capabilities.getPlatformCapability('LEETCODE').sourceApprovalStatus, 'unverified');
  assert.equal(capabilities.getPlatformCapability('CODECHEF').statisticsSourceTag, 'CODECHEF_THIRD_PARTY');
  assert.equal(capabilities.getPlatformCapability('CODECHEF').sourceApprovalStatus, 'unverified');
  assert.equal(capabilities.getStatsSourceKind('CODECHEF_THIRD_PARTY'), 'third-party');
  assert.equal(capabilities.getStatsSourceKind('GEEKSFORGEEKS_THIRD_PARTY'), 'unknown');
});

test('profile links use canonical HTTPS hosts and safely encode saved handles', () => {
  const url = capabilities.getPlatformCapability('CODEFORCES').profileUrlFor('a b');
  assert.equal(url, 'https://codeforces.com/profile/a%20b');
  assert.equal(new URL(url).protocol, 'https:');
  assert.equal(capabilities.getPlatformCapability('ATCODER').profileUrlFor, null);
});
