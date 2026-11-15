import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/utils/contestCalendarMonth.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);

const {
  CONTEST_PLATFORMS,
  buildMonthWeeks,
  filterContestsByPlatform,
  formatContestDateTime,
  formatContestDuration,
  formatMonthLabel,
  getCurrentMonth,
  getDayOfMonth,
  getPlatformAccent,
  getPlatformLabel,
  getTodayDateKey,
  groupContestsByDate,
  isSafeHttpUrl,
  parseContestMonthCalendarResponse,
  shiftMonth,
  toDateKey,
} = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

function contest(overrides = {}) {
  return {
    id: '1',
    platform: 'CODEFORCES',
    platformDisplayName: 'Codeforces',
    title: 'Codeforces Round 999',
    startTimeUtc: '2026-11-15T14:30:00Z',
    endTimeUtc: '2026-11-15T16:30:00Z',
    durationSeconds: 7200,
    contestUrl: 'https://codeforces.com/contest/1001',
    status: 'UPCOMING',
    ...overrides,
  };
}

function payload(overrides = {}) {
  return {
    year: 2026,
    month: 11,
    timeZone: 'Asia/Kolkata',
    generatedAt: '2026-11-01T00:00:00Z',
    contests: [contest()],
    ...overrides,
  };
}

test('all eight supported platforms are declared with distinct accents and labels', () => {
  assert.deepEqual(
    CONTEST_PLATFORMS.map((platform) => platform.code),
    ['CODEFORCES', 'LEETCODE', 'CODECHEF', 'ATCODER', 'GFG', 'CODE360', 'HACKERRANK', 'HACKEREARTH'],
  );
  assert.equal(new Set(CONTEST_PLATFORMS.map((platform) => platform.accent)).size, 8);
  assert.equal(getPlatformLabel('CODE360'), 'Code360');
  assert.equal(getPlatformLabel('HACKEREARTH'), 'HackerEarth');
  assert.equal(getPlatformAccent('GFG'), '#2f8d46');
  assert.equal(getPlatformLabel('UNKNOWN'), 'UNKNOWN');
});

test('parseContestMonthCalendarResponse accepts a valid payload', () => {
  const parsed = parseContestMonthCalendarResponse(payload());
  assert.equal(parsed.year, 2026);
  assert.equal(parsed.month, 11);
  assert.equal(parsed.timeZone, 'Asia/Kolkata');
  assert.equal(parsed.contests.length, 1);
  assert.deepEqual(parsed.contests[0], contest());
});

test('parseContestMonthCalendarResponse blanks unsafe contest URLs', () => {
  const parsed = parseContestMonthCalendarResponse(payload({
    contests: [contest({ contestUrl: 'javascript:alert(1)' })],
  }));
  assert.equal(parsed.contests[0].contestUrl, '');
});

test('parseContestMonthCalendarResponse rejects malformed payloads and events', () => {
  assert.throws(() => parseContestMonthCalendarResponse(null));
  assert.throws(() => parseContestMonthCalendarResponse(payload({ month: 13 })));
  assert.throws(() => parseContestMonthCalendarResponse(payload({ generatedAt: 'not-a-date' })));
  assert.throws(() => parseContestMonthCalendarResponse(payload({
    contests: [contest({ platform: 'HACKERX' })],
  })));
  assert.throws(() => parseContestMonthCalendarResponse(payload({
    contests: [contest({ startTimeUtc: 'not-a-date' })],
  })));
  assert.throws(() => parseContestMonthCalendarResponse(payload({
    contests: [contest({ durationSeconds: -1 })],
  })));
  assert.throws(() => parseContestMonthCalendarResponse(payload({
    contests: [contest({ status: 'CANCELLED' })],
  })));
});

test('isSafeHttpUrl only allows absolute http and https links', () => {
  assert.equal(isSafeHttpUrl('https://codeforces.com/contest/1'), true);
  assert.equal(isSafeHttpUrl('http://codechef.com/starters'), true);
  assert.equal(isSafeHttpUrl(' javascript:alert(1)'), false);
  assert.equal(isSafeHttpUrl('ftp://codeforces.com/contest/1'), false);
  assert.equal(isSafeHttpUrl('//codeforces.com/contest/1'), false);
  assert.equal(isSafeHttpUrl('/contest/1'), false);
  assert.equal(isSafeHttpUrl(''), false);
  assert.equal(isSafeHttpUrl(null), false);
});

test('buildMonthWeeks returns six full weeks of cells', () => {
  const weeks = buildMonthWeeks(2026, 11);
  assert.equal(weeks.length, 6);
  assert.ok(weeks.every((week) => week.length === 7));
  assert.equal(weeks[0][0], '2026-11-01');
  assert.equal(weeks[0][6], '2026-11-07');
  assert.equal(weeks[4][0], '2026-11-29');
  assert.equal(weeks[4][1], '2026-11-30');
  assert.equal(weeks[4][2], null);
  assert.equal(weeks[5].every((cell) => cell === null), true);
});

test('buildMonthWeeks pads the leading days for months that do not start on Sunday', () => {
  const weeks = buildMonthWeeks(2026, 9);
  assert.equal(weeks[0][0], null);
  assert.equal(weeks[0][1], null);
  assert.equal(weeks[0][2], '2026-09-01');
  assert.equal(weeks[0][6], '2026-09-05');
});

test('buildMonthWeeks handles February in a non-leap year', () => {
  const weeks = buildMonthWeeks(2026, 2);
  const days = weeks.flat().filter(Boolean);
  assert.equal(days.length, 28);
  assert.equal(days[0], '2026-02-01');
  assert.equal(days[27], '2026-02-28');
});

test('calendar date helpers produce stable keys and labels', () => {
  assert.equal(getDayOfMonth('2026-11-15'), 15);
  assert.equal(toDateKey('2026-11-15T14:30:00Z'), '2026-11-15');
  assert.equal(toDateKey('2026-11-15T20:30:00Z'), '2026-11-16');
  assert.equal(toDateKey('not-a-date'), null);
  assert.equal(formatMonthLabel(2026, 11), 'November 2026');
  assert.equal(getTodayDateKey('Asia/Kolkata', new Date('2026-11-15T20:30:00Z')), '2026-11-16');
});

test('groupContestsByDate buckets by IST day and sorts within each day', () => {
  const grouped = groupContestsByDate([
    contest({ id: 'late', startTimeUtc: '2026-11-15T16:30:00Z' }),
    contest({ id: 'early', startTimeUtc: '2026-11-15T04:30:00Z' }),
    contest({ id: 'next-day', startTimeUtc: '2026-11-15T20:30:00Z' }),
  ]);

  assert.deepEqual([...grouped.keys()], ['2026-11-15', '2026-11-16']);
  assert.deepEqual(grouped.get('2026-11-15').map((item) => item.id), ['early', 'late']);
  assert.deepEqual(grouped.get('2026-11-16').map((item) => item.id), ['next-day']);
});

test('filterContestsByPlatform treats an empty selection as all platforms', () => {
  const contests = [
    contest({ id: '1', platform: 'CODEFORCES' }),
    contest({ id: '2', platform: 'LEETCODE' }),
    contest({ id: '3', platform: 'GFG' }),
  ];

  assert.equal(filterContestsByPlatform(contests, []).length, 3);
  assert.deepEqual(
    filterContestsByPlatform(contests, ['LEETCODE']).map((item) => item.id),
    ['2'],
  );
  assert.deepEqual(
    filterContestsByPlatform(contests, ['codeforces', 'GFG']).map((item) => item.id),
    ['1', '3'],
  );
});

test('shiftMonth rolls across year boundaries in both directions', () => {
  assert.deepEqual(shiftMonth(2026, 11, 1), { year: 2026, month: 12 });
  assert.deepEqual(shiftMonth(2026, 12, 1), { year: 2027, month: 1 });
  assert.deepEqual(shiftMonth(2026, 1, -1), { year: 2025, month: 12 });
  assert.deepEqual(shiftMonth(2026, 11, -11), { year: 2025, month: 12 });
});

test('getCurrentMonth resolves the month in IST, not UTC', () => {
  assert.deepEqual(getCurrentMonth('Asia/Kolkata', new Date('2026-10-31T20:00:00Z')), { year: 2026, month: 11 });
  assert.deepEqual(getCurrentMonth('Asia/Kolkata', new Date('2026-11-30T18:00:00Z')), { year: 2026, month: 11 });
  assert.deepEqual(getCurrentMonth('Asia/Kolkata', new Date('2026-11-30T18:31:00Z')), { year: 2026, month: 12 });
});

test('formatContestDateTime renders an exact IST date and time', () => {
  assert.equal(formatContestDateTime('2026-11-15T14:30:00Z'), '15 Nov 2026, 8:00 PM IST');
  assert.equal(formatContestDateTime('2026-11-15T18:35:00Z'), '16 Nov 2026, 12:05 AM IST');
  assert.equal(formatContestDateTime('not-a-date'), 'Time unavailable');
});

test('formatContestDuration renders hours and minutes', () => {
  assert.equal(formatContestDuration(7200), '2h 00m');
  assert.equal(formatContestDuration(9000), '2h 30m');
  assert.equal(formatContestDuration(2700), '45m');
  assert.equal(formatContestDuration(0), '0m');
  assert.equal(formatContestDuration(-10), '0m');
  assert.equal(formatContestDuration(Number.NaN), '0m');
});
