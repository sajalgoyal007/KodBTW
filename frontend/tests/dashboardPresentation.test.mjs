import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/utils/dashboardPresentation.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(
  diagnostics.filter((diagnostic) => diagnostic.category === ts.DiagnosticCategory.Error),
  [],
);
const {
  getDashboardDisplayState,
  getDifficultyCoverage,
  getKnownDifficultyPercentage,
  getKnownPlatformSharePercentage,
  getPlatformSyncPresentation,
  hasAvailablePlatformStats,
  hasPartialPlatformStats,
} = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

test('dashboard states distinguish no accounts from connected accounts without available stats', () => {
  const base = { loading: false, error: null, hasStatsResponse: true, hasAvailableStats: false };
  assert.equal(getDashboardDisplayState({ ...base, hasConnectedAccounts: false, accountsLoaded: true }), 'no-accounts');
  assert.equal(getDashboardDisplayState({ ...base, hasConnectedAccounts: false, accountsLoaded: true, accountsLoadError: true }), 'accounts-unconfirmed');
  assert.equal(getDashboardDisplayState({ ...base, hasConnectedAccounts: true, accountsLoaded: true }), 'connected-unavailable');
  assert.equal(getDashboardDisplayState({ ...base, hasConnectedAccounts: false, accountsLoaded: false }), 'accounts-unconfirmed');
  assert.equal(getDashboardDisplayState({ ...base, hasConnectedAccounts: true, hasAvailableStats: true, accountsLoaded: true }), 'ready');
  assert.equal(getDashboardDisplayState({ ...base, loading: true, hasConnectedAccounts: false, accountsLoaded: false }), 'loading');
  assert.equal(getDashboardDisplayState({ ...base, error: 'failed', hasStatsResponse: false, hasConnectedAccounts: false, accountsLoaded: false }), 'error');
});

test('available and partial platform stats respect null versus zero', () => {
  const stats = {
    totalProblemsSolved: 0, easySolved: null, mediumSolved: null, hardSolved: null,
    rating: null, maxRating: null, rank: null, contestsParticipated: null,
    currentStreak: null, longestStreak: null,
  };
  assert.equal(hasAvailablePlatformStats(stats), true);
  assert.equal(hasPartialPlatformStats([stats]), true);
  assert.equal(hasPartialPlatformStats([], 1), true);
  assert.equal(hasAvailablePlatformStats({ ...stats, totalProblemsSolved: null }), false);
});

test('unknown share is unavailable when total solved is null but confirmed zero is preserved', () => {
  assert.equal(getKnownPlatformSharePercentage({ totalSolved: null, sharePercentage: 0, platform: 'CODECHEF' }), null);
  assert.equal(getKnownPlatformSharePercentage({ totalSolved: 0, sharePercentage: 0, platform: 'CODECHEF' }), 0);
  assert.equal(getKnownPlatformSharePercentage({ totalSolved: 5, sharePercentage: null, platform: 'CODECHEF' }), null);
  assert.equal(getKnownPlatformSharePercentage({ totalSolved: 5, sharePercentage: 12.5, platform: 'CODECHEF' }), 12.5);
});

test('missing difficulty counts are unavailable while confirmed zero remains zero', () => {
  assert.equal(getKnownDifficultyPercentage({ count: null, percentage: 0 }), null);
  assert.equal(getKnownDifficultyPercentage({ count: 0, percentage: 0 }), 0);
  assert.equal(getKnownDifficultyPercentage({ count: 2, percentage: null }), null);
});

test('difficulty summary reports unavailable, partial, and complete coverage accurately', () => {
  assert.deepEqual(getDifficultyCoverage(null, 0), { complete: false, message: 'Difficulty details are not available yet.' });
  assert.equal(getDifficultyCoverage({ platformBreakdown: [{ easy: 1, medium: null, hard: 2 }] }, 1).complete, false);
  assert.equal(getDifficultyCoverage({ platformBreakdown: [{ easy: 1, hard: 2 }] }, 1).complete, false);
  assert.equal(getDifficultyCoverage({ platformBreakdown: [{ easy: 1, medium: 2, hard: 3 }] }, 2).complete, false);
  assert.deepEqual(getDifficultyCoverage({ platformBreakdown: [{ easy: 1, medium: 2, hard: 3 }] }, 1), {
    complete: true,
    message: 'Difficulty counts are available for all 1 connected profile.',
  });
});

test('sync status distinguishes current, possibly stale, failed, running, never-synced, and unavailable states', () => {
  const base = { syncStatus: 'SUCCEEDED', sourceStatus: 'SYNCED', fresh: true, lastSuccessAt: '2026-10-10T10:00:00' };
  assert.equal(getPlatformSyncPresentation(base).label, 'Recently synced');
  assert.equal(getPlatformSyncPresentation({ ...base, fresh: false }).label, 'May be stale');
  assert.equal(getPlatformSyncPresentation({ ...base, syncStatus: 'FAILED' }).detail, 'Showing previously saved stats; they may be stale.');
  assert.equal(getPlatformSyncPresentation({ ...base, syncStatus: 'FAILED', lastSuccessAt: null }).detail, 'No successful statistics have been saved yet.');
  assert.equal(getPlatformSyncPresentation({ ...base, syncStatus: 'RUNNING' }).label, 'Sync in progress');
  assert.equal(getPlatformSyncPresentation({ ...base, syncStatus: 'NEVER_SYNCED', lastSuccessAt: null }).label, 'Never synced');
  const pendingNeverSynced = getPlatformSyncPresentation({ ...base, syncStatus: 'NEVER_SYNCED', sourceStatus: 'SOURCE_PENDING', lastSuccessAt: null });
  assert.equal(pendingNeverSynced.label, 'Live statistics unavailable');
  assert.match(pendingNeverSynced.detail, /no successful sync is recorded/);
});
