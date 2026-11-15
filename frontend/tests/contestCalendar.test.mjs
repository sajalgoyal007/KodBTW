import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import ts from 'typescript';

const source = await readFile(new URL('../src/utils/contestCalendar.ts', import.meta.url), 'utf8');
const { outputText, diagnostics } = ts.transpileModule(source, {
  compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  reportDiagnostics: true,
});
assert.deepEqual(diagnostics.filter((item) => item.category === ts.DiagnosticCategory.Error), []);
const {
  filterAndSortContests,
  formatContestDuration,
  formatContestLocalTime,
  getContestCalendarViewState,
  getContestCountdown,
  markContestCalendarStale,
  parseContestCalendarResponse,
} = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

const response = {
  generatedAt: '2026-10-11T12:00:00Z',
  stale: false,
  sources: [{
    platform: 'CODEFORCES', status: 'AVAILABLE', message: null,
    fetchedAt: '2026-10-11T12:00:00Z', cacheExpiresAt: '2026-10-11T12:10:00Z',
    source: 'CODEFORCES_API',
  }],
  contests: [
    { id: '2', platform: 'CODEFORCES', name: 'Later', startTime: '2026-10-13T12:00:00Z', durationSeconds: 5400, officialUrl: 'https://codeforces.com/contest/2', source: 'CODEFORCES_API', fetchedAt: '2026-10-11T12:00:00Z' },
    { id: '1', platform: 'CODEFORCES', name: 'Earlier', startTime: '2026-10-12T12:00:00Z', durationSeconds: 7200, officialUrl: 'https://codeforces.com/contest/1', source: 'CODEFORCES_API', fetchedAt: '2026-10-11T12:00:00Z' },
    { id: '3', platform: 'LEETCODE', name: 'Other source', startTime: '2026-10-11T14:00:00Z', durationSeconds: 3600, officialUrl: 'https://leetcode.com/contest/', source: 'OTHER', fetchedAt: '2026-10-11T12:00:00Z' },
  ],
};

test('validates API responses and rejects malformed contest data', () => {
  assert.equal(parseContestCalendarResponse(response).contests.length, 3);
  assert.equal(parseContestCalendarResponse(response).sources[0].source, 'CODEFORCES_API');
  assert.throws(() => parseContestCalendarResponse({ ...response, contests: [{ ...response.contests[0], startTime: 'invalid' }] }), /response is invalid|event is invalid/);
  assert.throws(() => parseContestCalendarResponse({ ...response, sources: [{ platform: 'CODEFORCES', status: 'BROKEN' }] }), /source status is invalid/);
});

test('filters by platform and sorts start times without mutating the response', () => {
  const all = filterAndSortContests(response.contests, 'ALL', 'ALL_UPCOMING', Date.parse('2026-10-11T13:00:00Z'));
  assert.deepEqual(all.map((contest) => contest.id), ['3', '1', '2']);
  assert.deepEqual(filterAndSortContests(response.contests, 'CODEFORCES', 'ALL_UPCOMING', Date.parse('2026-10-11T13:00:00Z')).map((contest) => contest.id), ['1', '2']);
  assert.equal(response.contests[0].id, '2');
});

test('time filters keep running contests, exclude ended events, sort, and remove duplicate IDs', () => {
  const now = Date.parse('2026-10-11T13:00:00Z');
  const events = [
    { ...response.contests[0], id: 'ended', startTime: '2026-10-11T10:00:00Z', durationSeconds: 3600 },
    { ...response.contests[0], id: 'running', name: 'Running', startTime: '2026-10-11T12:30:00Z', durationSeconds: 7200 },
    response.contests[1],
    { ...response.contests[1], name: 'Duplicate copy' },
    response.contests[0],
    { ...response.contests[0], id: 'far', startTime: '2026-11-20T12:00:00Z' },
  ];

  assert.deepEqual(filterAndSortContests(events, 'ALL', 'ALL_UPCOMING', now).map((item) => item.id), ['running', '1', '2', 'far']);
  assert.deepEqual(filterAndSortContests(events, 'ALL', 'NEXT_7_DAYS', now).map((item) => item.id), ['running', '1', '2']);
  assert.deepEqual(filterAndSortContests(events, 'ALL', 'NEXT_30_DAYS', now).map((item) => item.id), ['running', '1', '2']);
  assert.equal(filterAndSortContests(events, 'ALL', 'ALL_UPCOMING', now).find((item) => item.id === '1').name, 'Earlier');
});

test('formats event start in local time with an explicit timezone label', () => {
  const formatted = formatContestLocalTime('2026-10-12T12:00:00Z', 'Asia/Kolkata', 'en-US');
  assert.match(formatted.dateTime, /Oct 12, 2026/);
  assert.match(formatted.dateTime, /5:30 PM/);
  assert.match(formatted.dateTime, /GMT\+5:30/);
  assert.equal(formatted.timeZone, 'Asia/Kolkata');
  assert.equal(formatContestLocalTime('not-a-date').dateTime, 'Time unavailable');
});

test('countdown distinguishes upcoming, running, and ended contests', () => {
  const event = { startTime: '2026-10-11T12:00:00Z', durationSeconds: 3600 };
  assert.deepEqual(getContestCountdown(event, Date.parse('2026-10-11T11:59:00Z')), { state: 'upcoming', label: 'Starts in 1m 0s' });
  assert.deepEqual(getContestCountdown(event, Date.parse('2026-10-11T12:30:00Z')), { state: 'running', label: 'Started · ends in 30m 0s' });
  assert.deepEqual(getContestCountdown(event, Date.parse('2026-10-11T13:00:00Z')), { state: 'ended', label: 'Ended' });
  assert.equal(formatContestDuration(5_400), '1h 30m');
});

test('derives loading, empty, error, unavailable, stale, and populated states', () => {
  const base = { response, loading: false, error: null, visibleContestCount: 1 };
  assert.equal(getContestCalendarViewState({ ...base, response: null, loading: true }), 'loading');
  assert.equal(getContestCalendarViewState({ ...base, response: null, error: 'offline' }), 'error');
  assert.equal(getContestCalendarViewState({ ...base, response: { ...response, sources: [{ ...response.sources[0], status: 'UNAVAILABLE' }], contests: [] }, visibleContestCount: 0 }), 'unavailable');
  assert.equal(getContestCalendarViewState({ ...base, response: { ...response, contests: [] }, visibleContestCount: 0 }), 'empty');
  assert.equal(getContestCalendarViewState({ ...base, response: { ...response, stale: true } }), 'stale');
  assert.equal(getContestCalendarViewState(base), 'contests');
});

test('retained response after a failed refresh is marked stale without changing last successful fetch time', () => {
  const retained = markContestCalendarStale(response);
  assert.equal(retained.stale, true);
  assert.equal(retained.sources[0].status, 'STALE');
  assert.equal(retained.sources[0].fetchedAt, response.sources[0].fetchedAt);
  assert.equal(retained.contests.length, response.contests.length);
  assert.equal(getContestCalendarViewState({
    loading: false, error: 'refresh failed', response: retained, visibleContestCount: 2,
  }), 'error');
});
