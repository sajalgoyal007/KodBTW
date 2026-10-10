import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/utils/platformStatsSource.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(
  diagnostics.filter((diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error),
  [],
);
const {
  getDashboardRefreshFeedback,
  getPlatformAvailabilityLabel,
  getPlatformStatsSourceLabel,
  isVerifiedStatsSource,
} = await import(
  `data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`,
);

test('recognizes CodeChef third-party stats without accepting mock sources', () => {
  assert.equal(isVerifiedStatsSource('CODECHEF_THIRD_PARTY'), true);
  assert.equal(isVerifiedStatsSource('LEETCODE_REAL'), true);
  assert.equal(isVerifiedStatsSource('CODEFORCES_REAL'), true);
  assert.equal(isVerifiedStatsSource('MOCK'), false);
  assert.equal(isVerifiedStatsSource('CODECHEF_MOCK'), false);
  assert.equal(isVerifiedStatsSource('SOURCE_PENDING'), false);
  assert.equal(isVerifiedStatsSource(null), false);
});

test('CodeChef availability follows backend status and is not inferred from connection', () => {
  assert.equal(getPlatformAvailabilityLabel('CODECHEF'), 'Connect an account to check availability');
  assert.equal(getPlatformAvailabilityLabel('CODECHEF', { sourceStatus: 'SOURCE_PENDING' }), 'Live statistics unavailable');
  assert.equal(getPlatformAvailabilityLabel('CODECHEF', { sourceStatus: 'REAL_AVAILABLE' }), 'Live statistics available');
  assert.equal(getPlatformAvailabilityLabel('CODECHEF', { sourceStatus: 'SYNC_FAILED' }), 'Last sync failed');
  assert.equal(getPlatformAvailabilityLabel('CODECHEF', { sourceStatus: 'SYNCED' }), 'Live statistics synced');
});

test('dashboard source labels distinguish live, pending, mock, and unknown data', () => {
  assert.equal(getPlatformStatsSourceLabel('LEETCODE_REAL'), 'Live Public Data');
  assert.equal(getPlatformStatsSourceLabel('CODEFORCES_REAL'), 'Live Public Data');
  assert.equal(getPlatformStatsSourceLabel('CODECHEF_THIRD_PARTY'), 'Third-party Live Data');
  assert.equal(getPlatformStatsSourceLabel('SOURCE_PENDING'), 'Live statistics unavailable');
  assert.equal(getPlatformStatsSourceLabel('UNSYNCED'), 'Not synced');
  assert.equal(getPlatformStatsSourceLabel('UNSYNCED', 'LIVE_SYNC_UNAVAILABLE'), 'Live sync unavailable');
  assert.equal(getPlatformStatsSourceLabel('MOCK'), 'Mock Sandbox');
  assert.equal(getPlatformStatsSourceLabel('UNRECOGNIZED_SOURCE'), 'Statistics unavailable');
});

test('dashboard refresh reports success only for succeeded syncs with verified populated stats', () => {
  const currentStats = {
    source: 'CODECHEF_THIRD_PARTY',
    totalProblemsSolved: 656,
    rating: 1247,
    rank: 76970,
  };
  assert.deepEqual(getDashboardRefreshFeedback({
    syncStatus: { status: 'SUCCEEDED', failureMessage: null },
    currentStats,
    cooldownApplied: false,
  }), { success: true, message: 'Stats refreshed successfully.' });
  assert.deepEqual(getDashboardRefreshFeedback({
    syncStatus: { status: 'SUCCEEDED', failureMessage: null },
    currentStats,
    cooldownApplied: true,
  }), { success: true, message: 'Recently synced; showing saved stats.' });
});

test('never-synced or source-pending response is unavailable rather than successful', () => {
  assert.deepEqual(getDashboardRefreshFeedback({
    syncStatus: { status: 'NEVER_SYNCED', failureMessage: null },
    currentStats: { source: 'SOURCE_PENDING', totalProblemsSolved: null, rating: null, rank: null },
    cooldownApplied: false,
  }), {
    success: false,
    message: 'Live statistics are unavailable; no successful sync was completed.',
  });
});

test('failed response preserves the backend failure message', () => {
  assert.deepEqual(getDashboardRefreshFeedback({
    syncStatus: { status: 'FAILED', failureMessage: 'The platform temporarily limited requests. Try again later.' },
    currentStats: { source: 'CODECHEF_THIRD_PARTY', totalProblemsSolved: 656 },
    cooldownApplied: false,
  }), {
    success: false,
    message: 'The platform temporarily limited requests. Try again later.',
  });
});

test('mock, unavailable source, and missing metrics cannot produce refresh success', () => {
  const syncStatus = { status: 'SUCCEEDED', failureMessage: null };
  for (const currentStats of [
    { source: 'MOCK', totalProblemsSolved: 12 },
    { source: 'SOURCE_PENDING', totalProblemsSolved: 12 },
    { source: 'CODECHEF_THIRD_PARTY', totalProblemsSolved: null, rating: null, rank: null },
    null,
  ]) {
    assert.equal(getDashboardRefreshFeedback({ syncStatus, currentStats, cooldownApplied: false }).success, false);
  }
});
