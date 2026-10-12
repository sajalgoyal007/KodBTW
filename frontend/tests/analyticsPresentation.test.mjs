import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/utils/analyticsPresentation.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);
const {
  getAnalyticsMetricAvailability,
  getAnalyticsPageState,
  getDifficultyDistributionState,
  getHistoryViewState,
  getKnownStreakBarPercentage,
  getRetainedDataViewState,
} = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

test('analytics refresh keeps prior results visible and distinguishes a failed refresh', () => {
  assert.equal(getAnalyticsPageState({ loading: true, error: null, hasData: false, hasMetrics: false }), 'loading');
  assert.equal(getAnalyticsPageState({ loading: true, error: null, hasData: true, hasMetrics: true }), 'refreshing');
  assert.equal(getAnalyticsPageState({ loading: false, error: 'Refresh failed', hasData: true, hasMetrics: true }), 'stale-error');
  assert.equal(getAnalyticsPageState({ loading: false, error: null, hasData: true, hasMetrics: false }), 'empty');
});

test('history refresh preserves only current-range observations and marks a failed refresh stale', () => {
  assert.equal(getHistoryViewState({ loading: true, error: null, hasCurrentRangeData: false }), 'loading');
  assert.equal(getHistoryViewState({ loading: true, error: null, hasCurrentRangeData: true }), 'refreshing');
  assert.equal(getHistoryViewState({ loading: false, error: 'Unavailable', hasCurrentRangeData: true }), 'stale-error');
  assert.equal(getHistoryViewState({ loading: false, error: 'Unavailable', hasCurrentRangeData: false }), 'error');
});

test('insight and activity sections retain loaded data during refreshes and failures', () => {
  assert.equal(getRetainedDataViewState({ loading: true, error: null, hasData: false }), 'loading');
  assert.equal(getRetainedDataViewState({ loading: false, error: 'Unavailable', hasData: false }), 'error');
  assert.equal(getRetainedDataViewState({ loading: true, error: null, hasData: true }), 'refreshing');
  assert.equal(getRetainedDataViewState({ loading: false, error: 'Unavailable', hasData: true }), 'stale-error');
  assert.equal(getRetainedDataViewState({ loading: false, error: null, hasData: true }), 'content');
});

test('backend empty-response zeroes are not treated as user statistics when no profiles exist', () => {
  const emptyDifficulty = {
    totalProblemsSolved: 0,
    easy: { count: 0, percentage: 0 },
    medium: { count: 0, percentage: 0 },
    hard: { count: 0, percentage: 0 },
  };
  const emptyContests = { totalContests: 0, platformBreakdown: [] };
  assert.deepEqual(getAnalyticsMetricAvailability([], emptyDifficulty, emptyContests), {
    hasProfiles: false,
    hasSolvedMetrics: false,
    hasRatingMetrics: false,
    hasStreakMetrics: false,
    hasContestMetrics: false,
  });

  const profile = {
    platform: 'CODEFORCES', username: 'example', source: 'CODEFORCES_REAL',
    totalSolved: 0, sharePercentage: 0, rating: null, rank: null,
    contestsParticipated: null, currentStreak: null, longestStreak: null,
  };
  assert.equal(getAnalyticsMetricAvailability([profile], emptyDifficulty, emptyContests).hasSolvedMetrics, true);
});

test('difficulty chart is withheld for incomplete or inconsistent data, but a genuine zero total is explicit', () => {
  assert.equal(getDifficultyDistributionState(null), 'incomplete');
  assert.equal(getDifficultyDistributionState({ totalProblemsSolved: 8, easy: { count: 4, percentage: 50 }, medium: { count: null, percentage: null }, hard: { count: 4, percentage: 50 } }), 'incomplete');
  assert.equal(getDifficultyDistributionState({ totalProblemsSolved: 8, easy: { count: 4, percentage: 50 }, medium: { count: 2, percentage: 25 }, hard: { count: 2, percentage: 25 } }), 'available');
  assert.equal(getDifficultyDistributionState({ totalProblemsSolved: 8, easy: { count: 4, percentage: 40 }, medium: { count: 2, percentage: 30 }, hard: { count: 2, percentage: 30 } }), 'incomplete');
  assert.equal(getDifficultyDistributionState({ totalProblemsSolved: 8, easy: { count: 4, percentage: 50 }, medium: { count: 1, percentage: 25 }, hard: { count: 2, percentage: 25 } }), 'incomplete');
  assert.equal(getDifficultyDistributionState({ totalProblemsSolved: 0, easy: { count: 0, percentage: 0 }, medium: { count: 0, percentage: 0 }, hard: { count: 0, percentage: 0 } }), 'zero-total');
});

test('streak bars preserve real zero and omit unknown values', () => {
  assert.equal(getKnownStreakBarPercentage(null, 12), null);
  assert.equal(getKnownStreakBarPercentage(undefined, 12), null);
  assert.equal(getKnownStreakBarPercentage(0, 12), 0);
  assert.equal(getKnownStreakBarPercentage(6, 12), 50);
  assert.equal(getKnownStreakBarPercentage(16, 12), 100);
});
