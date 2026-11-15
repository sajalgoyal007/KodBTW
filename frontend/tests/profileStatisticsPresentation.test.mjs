import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/utils/profileStatisticsPresentation.ts', import.meta.url), 'utf8');
const compiled = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(compiled.diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);

const { getSupportedProfileMetrics, getProfileSyncMessage, getProfileAchievements } = await import(
  `data:text/javascript;base64,${Buffer.from(compiled.outputText).toString('base64')}`
);

const account = (overrides = {}) => ({
  syncStatus: 'SUCCEEDED', sourceStatus: 'SYNCED', fresh: true, lastSuccessAt: '2026-10-19T10:00:00',
  ...overrides,
});

const stats = (overrides = {}) => ({
  platform: 'CODEFORCES', username: 'candidate', source: 'CODEFORCES_REAL',
  totalProblemsSolved: null, easySolved: null, mediumSolved: null, hardSolved: null,
  rating: 0, maxRating: 1450, rank: null, contestsParticipated: null,
  currentStreak: null, longestStreak: null, lastSyncedAt: '2026-10-19T10:00:00',
  ...overrides,
});

test('supported metric list preserves genuine zero and leaves missing values unavailable', () => {
  const metrics = getSupportedProfileMetrics(stats(), ['rating', 'maxRating'], true);
  assert.deepEqual(metrics.map(({ key }) => key), ['rating', 'maxRating']);
  assert.equal(metrics[0].value, 0);
  assert.equal(metrics[1].value, 1450);
  assert.deepEqual(getSupportedProfileMetrics(stats({ source: 'SOURCE_PENDING' }), ['rating'], false), []);
  assert.deepEqual(getSupportedProfileMetrics(stats({ source: null }), ['rating'], false), []);
  assert.equal(getSupportedProfileMetrics(undefined, ['rating'], true).length, 0);

  const partial = getSupportedProfileMetrics(stats({ rating: null, maxRating: 900 }), ['rating', 'maxRating'], true);
  assert.deepEqual(partial.map(({ value }) => value), [null, 900]);
  assert.deepEqual(getSupportedProfileMetrics(stats({ totalProblemsSolved: 500 }), ['rating', 'maxRating'], true)
    .map(({ key }) => key), ['rating', 'maxRating']);
});

test('sync messaging distinguishes pending, in-progress, failed-retained and stale snapshots', () => {
  const retainedStats = stats({ totalProblemsSolved: 42 });
  assert.equal(getSupportedProfileMetrics(retainedStats, ['totalProblemsSolved'], true)[0].value, 42);
  assert.match(getProfileSyncMessage(account({ syncStatus: 'FAILED' }), true), /previously saved.*retained.*stale/i);
  assert.match(getProfileSyncMessage(account({ syncStatus: 'FAILED', lastSuccessAt: null }), false), /no successful/i);
  assert.match(getProfileSyncMessage(account({ syncStatus: 'RUNNING' }), true), /refresh is running.*snapshot remains visible/i);
  assert.match(getProfileSyncMessage(account({ syncStatus: 'SUCCEEDED', fresh: false }), true), /may be stale/i);
  assert.match(getProfileSyncMessage(account({ sourceStatus: 'SOURCE_PENDING', syncStatus: 'NEVER_SYNCED' }), false), /no enabled live statistics source/i);
});

test('link-only accounts keep the saved link and expose no statistics metrics', () => {
  const linkOnly = account({ sourceStatus: 'LINK_ONLY', syncStatus: 'NEVER_SYNCED', lastSuccessAt: null, fresh: false });
  assert.match(getProfileSyncMessage(linkOnly, false), /statistics are not fetched or tracked/i);
  assert.deepEqual(getSupportedProfileMetrics(stats({ source: 'LINK_ONLY' }), [], false), []);
  assert.deepEqual(
    getSupportedProfileMetrics(stats({ source: 'ALGOMASTER_LINK_ONLY', totalProblemsSolved: 99 }), ['totalProblemsSolved'], false),
    [],
  );
});

test('achievements use eligible source data, never combine solves across platforms, and do not unlock from missing data', () => {
  const base = getProfileAchievements([], 0);
  assert.equal(base[0].unlocked, false);
  assert.equal(base[1].unlocked, false);
  assert.equal(base[2].unlocked, false);

  const partial = getProfileAchievements([
    { displayName: 'Codeforces', totalProblemsSolved: 60 },
    { displayName: 'CodeChef', totalProblemsSolved: 50 },
  ], 2);
  assert.equal(partial[0].unlocked, true);
  assert.equal(partial[1].unlocked, true);
  assert.equal(partial[2].unlocked, false);

  const milestone = getProfileAchievements([{ displayName: 'Codeforces', totalProblemsSolved: 100 }], 1);
  assert.equal(milestone[2].unlocked, true);
  assert.match(milestone[2].evidence, /Codeforces reports 100 solved/);
});
