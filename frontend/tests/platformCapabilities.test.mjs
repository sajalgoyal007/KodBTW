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

const backend = await readFile(new URL('../../backend/src/main/java/com/kodbtw/entity/Platform.java', import.meta.url), 'utf8');

test('account-connectable platforms match the backend enum without adding fake connections', async () => {
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

test('all ten platforms are modelled and nine expose live statistics', () => {
  assert.equal(capabilities.PLATFORM_CAPABILITIES.length, 10);
  const live = capabilities.PLATFORM_CAPABILITIES.filter((item) => item.synchronizationEnabled);
  assert.equal(live.length, 9);
  assert.deepEqual(live.map((item) => item.id).sort(), [
    'ATCODER',
    'CODE360',
    'CODECHEF',
    'CODEFORCES',
    'GEEKSFORGEEKS',
    'HACKEREARTH',
    'HACKERRANK',
    'INTERVIEWBIT',
    'LEETCODE',
  ]);
});

test('AlgoMaster is link-only and never advertises fetched metrics', () => {
  const capability = capabilities.getPlatformCapability('ALGOMASTER');
  assert.equal(capability.accountConnectionAvailable, true);
  assert.equal(capability.synchronizationEnabled, false);
  assert.equal(capability.liveStatisticsAvailable, false);
  assert.deepEqual(capability.supportedMetrics, []);
  assert.equal(capability.statisticsSourceTag, null);
  assert.match(capability.availabilityMessage, /link-only/i);

  assert.equal(capabilities.getPlatformConnectionStatus('ALGOMASTER', 'LINK_ONLY'), 'Profile link saved · statistics not tracked');
  assert.equal(capabilities.getStatsSourceKind('ALGOMASTER_LINK_ONLY'), 'link-only');
  assert.equal(capabilities.getStatsSourceKind('LINK_ONLY'), 'link-only');
});

test('capabilities describe adapter metrics and provenance for every new source', () => {
  assert.deepEqual(capabilities.getPlatformCapability('CODEFORCES').supportedMetrics, ['rating', 'maxRating']);
  assert.equal(capabilities.getPlatformCapability('CODEFORCES').sourceApprovalStatus, 'documented-public-api');
  assert.equal(capabilities.getPlatformCapability('LEETCODE').sourceApprovalStatus, 'unverified');

  assert.equal(capabilities.getPlatformCapability('CODECHEF').statisticsSourceTag, 'CODECHEF_THIRD_PARTY');
  assert.equal(capabilities.getPlatformCapability('GEEKSFORGEEKS').statisticsSourceTag, 'GEEKSFORGEEKS_THIRD_PARTY');
  assert.equal(capabilities.getPlatformCapability('CODE360').statisticsSourceTag, 'CODE360_THIRD_PARTY');
  assert.equal(capabilities.getPlatformCapability('ATCODER').statisticsSourceTag, 'ATCODER_REAL');
  assert.equal(capabilities.getPlatformCapability('HACKERRANK').statisticsSourceTag, 'HACKERRANK_REAL');
  assert.equal(capabilities.getPlatformCapability('INTERVIEWBIT').statisticsSourceTag, 'INTERVIEWBIT_REAL');
  assert.equal(capabilities.getPlatformCapability('HACKEREARTH').statisticsSourceTag, 'HACKEREARTH_REAL');

  for (const platform of ['CODECHEF', 'GEEKSFORGEEKS', 'CODE360']) {
    assert.equal(capabilities.getPlatformCapability(platform).sourceApprovalStatus, 'unverified');
  }

  assert.equal(capabilities.getStatsSourceKind('CODECHEF_THIRD_PARTY'), 'third-party');
  assert.equal(capabilities.getStatsSourceKind('GEEKSFORGEEKS_THIRD_PARTY'), 'third-party');
  assert.equal(capabilities.getStatsSourceKind('CODE360_THIRD_PARTY'), 'third-party');
  assert.equal(capabilities.getStatsSourceKind('ATCODER_REAL'), 'live');
  assert.equal(capabilities.getStatsSourceKind('HACKERRANK_REAL'), 'live');
  assert.equal(capabilities.getStatsSourceKind('INTERVIEWBIT_REAL'), 'live');
  assert.equal(capabilities.getStatsSourceKind('HACKEREARTH_REAL'), 'live');
  assert.equal(capabilities.getStatsSourceKind('UNKNOWN_REAL'), 'unknown');
});

test('profile links use canonical HTTPS hosts and safely encode saved handles', () => {
  const url = capabilities.getPlatformCapability('CODEFORCES').profileUrlFor('a b');
  assert.equal(url, 'https://codeforces.com/profile/a%20b');
  assert.equal(new URL(url).protocol, 'https:');

  const expected = {
    LEETCODE: 'https://leetcode.com/u/coder/',
    CODECHEF: 'https://www.codechef.com/users/coder',
    ATCODER: 'https://atcoder.jp/users/coder',
    GEEKSFORGEEKS: 'https://www.geeksforgeeks.org/profile/coder',
    CODE360: 'https://www.naukri.com/code360/profile/coder',
    HACKERRANK: 'https://www.hackerrank.com/profile/coder',
    INTERVIEWBIT: 'https://www.interviewbit.com/profile/coder/',
    HACKEREARTH: 'https://www.hackerearth.com/@coder',
  };
  for (const [platform, link] of Object.entries(expected)) {
    assert.equal(capabilities.getPlatformCapability(platform).profileUrlFor('coder'), link, platform);
  }
  assert.equal(capabilities.getPlatformCapability('ALGOMASTER').profileUrlFor, null);
});
