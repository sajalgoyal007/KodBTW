import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/utils/historyChart.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);
const {
  getDatePositionRatio,
  getHistoryDateMidpoint,
  getHistoryResponseDateError,
  getValidatedHistoryDateDomain,
  splitObservedSegments,
} = await import(
  `data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`,
);

test('chart x positions use calendar distance within the server-provided range', () => {
  const domain = { startDate: '2024-02-01', endDate: '2024-02-29' };
  assert.equal(getDatePositionRatio('2024-02-01', domain), 0);
  assert.equal(getDatePositionRatio('2024-02-15', domain), 14 / 28);
  assert.equal(getDatePositionRatio('2024-02-29', domain), 1);
  assert.equal(getDatePositionRatio('not-a-date', domain), null);
  assert.equal(getDatePositionRatio('2024-01-31', domain), null);
  assert.equal(getHistoryDateMidpoint(domain), '2024-02-15');
  assert.doesNotThrow(() => getHistoryDateMidpoint({ startDate: 'invalid', endDate: '2024-02-29' }));
  assert.equal(getHistoryDateMidpoint({ startDate: 'invalid', endDate: '2024-02-29' }), null);
  assert.notEqual(
    getDatePositionRatio('2024-02-15', { startDate: '2024-02-23', endDate: '2024-02-29' }),
    getDatePositionRatio('2024-02-15', domain),
  );
});

test('rejects missing, invalid, reversed, empty, and selected-range-inconsistent bounds', () => {
  for (const [range, start, end] of [
    ['7d', undefined, '2024-02-29'],
    ['7d', '2024-02-23', undefined],
    ['7d', '', '2024-02-29'],
    ['7d', '2024-02-30', '2024-03-06'],
    ['7d', '2024-02-29', '2024-02-23'],
    ['7d', '2024-02-22', '2024-02-29'],
    ['30d', '2024-02-23', '2024-02-29'],
  ]) {
    assert.equal(getValidatedHistoryDateDomain(range, start, end), null);
  }
});

test('accepts exact UTC bounds for all ranges, including leap-year year subtraction', () => {
  const end = '2024-02-29';
  assert.deepEqual(getValidatedHistoryDateDomain('7d', '2024-02-23', end), { startDate: '2024-02-23', endDate: end });
  assert.deepEqual(getValidatedHistoryDateDomain('30d', '2024-01-31', end), { startDate: '2024-01-31', endDate: end });
  assert.deepEqual(getValidatedHistoryDateDomain('90d', '2023-12-02', end), { startDate: '2023-12-02', endDate: end });
  assert.deepEqual(getValidatedHistoryDateDomain('1y', '2023-02-28', end), { startDate: '2023-02-28', endDate: end });
});

test('rejects malformed or out-of-domain observation dates before charts render', () => {
  const valid = {
    range: '7d', rangeStart: '2024-02-23', rangeEnd: '2024-02-29',
    snapshotDates: ['2024-02-29'], overallSolved: [{ date: '2024-02-29' }],
    platforms: [{ platform: 'CODECHEF', points: [{ date: '2024-02-29' }] }],
    freshness: { latestSnapshotDate: '2024-02-29', lastUpdatedAt: null },
  };
  assert.equal(getHistoryResponseDateError(valid, '7d'), null);
  assert.doesNotThrow(() => getHistoryResponseDateError({ ...valid, rangeStart: undefined }, '7d'));
  assert.doesNotThrow(() => getHistoryResponseDateError({ ...valid, rangeEnd: '' }, '7d'));
  assert.notEqual(getHistoryResponseDateError({ ...valid, snapshotDates: ['bad-date'] }, '7d'), null);
  assert.notEqual(getHistoryResponseDateError({ ...valid, overallSolved: [{ date: '2024-02-22' }] }, '7d'), null);
  assert.notEqual(getHistoryResponseDateError({ ...valid, platforms: [{ platform: 'X', points: [{ date: '' }] }] }, '7d'), null);
  assert.notEqual(getHistoryResponseDateError({ ...valid, range: '30d' }, '7d'), null);
  assert.notEqual(getHistoryResponseDateError({ ...valid, freshness: null }, '7d'), null);
});

test('observed segments break at null values and missing calendar dates without adding points', () => {
  const points = [
    { date: '2024-01-01', value: 10 },
    { date: '2024-01-02', value: 11 },
    { date: '2024-01-04', value: 12 },
    { date: '2024-01-05', value: null },
    { date: '2024-01-06', value: 13 },
  ];
  const segments = splitObservedSegments(points);
  assert.deepEqual(segments, [[points[0], points[1]], [points[2]], [points[4]]]);
  assert.equal(segments.flat().length, 4);
});
