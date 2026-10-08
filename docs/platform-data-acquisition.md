# Platform data acquisition

Research and ordinary unauthenticated HTTP probes performed **8 October 2026**. These probes checked public-page availability only. No credentials, cookies, browser automation, XHR reverse engineering, or access-control bypasses were used. A page returning HTTP 200 does not establish permission for automated extraction.

## Platform findings

### CodeChef

| Field | Finding |
|---|---|
| Platform | CodeChef |
| Source | Public user pages: `https://www.codechef.com/users/{username}`. No current official public user-stat API contract was located. |
| Source type | Public profile HTML; profile page, not a documented data API. |
| Official/Unofficial | Public page is official; any extracted data endpoint would be undocumented/unverified. |
| Authentication required? | No login was needed for the successful ordinary GET probe. |
| Publicly accessible? | Yes for tested handle `lee215` (HTTP 200, `text/html`, page title `lee215 | CodeChef`). The `tourist` probe did not receive an HTTP response in this environment. |
| Metrics available | **UNKNOWN** for machine-readable acquisition. The page is human-facing; this review did not parse its contents. |
| Metrics unavailable | **UNKNOWN** for individual metrics from a permitted API. |
| Request method | `GET` for a public profile page. No implementation request will be made. |
| Example request | `GET https://www.codechef.com/users/lee215` |
| Example response shape | Probe: `200 text/html; charset=utf-8` for `lee215`; no page data was extracted. |
| Rate-limit observations | No public automated-access rate limit was verified. Do not infer permission from lack of a published limit. |
| Failure modes | Probe failures/network timeout; unverified not-found/private-profile, rate-limit, and page-change responses. Do not bypass any challenge or access restriction. |
| Terms/compliance concern | CodeChef’s current Terms prohibit spidering, crawling, and scraping in Prohibited Uses, and Section 2 restricts reproducing/copying/exploiting its service without prior written permission. See [Terms of Service](https://www.codechef.com/terms). |
| Reliability | Low for automated use; HTML is not a supported contract, and extraction is restricted by the terms. |
| Implementation decision | Keep `LIVE_SYNC_UNAVAILABLE`. Do not parse HTML or reverse engineer endpoints absent written permission or an officially documented public API. |

### GeeksforGeeks

| Field | Finding |
|---|---|
| Platform | GeeksforGeeks |
| Source | Public profile pages: `https://www.geeksforgeeks.org/user/{username}/`. A third-party API was also found at `https://gfg-stats.tashif.codes/{username}`. |
| Source type | Official public profile HTML; third-party unauthenticated JSON API with unverified upstream/provenance. |
| Official/Unofficial | Profile route is official. `gfg-stats.tashif.codes` is **unofficial** and is not operated or documented by GeeksforGeeks. |
| Authentication required? | No login for tested profile pages or the third-party API. |
| Publicly accessible? | Yes: `geeksforgeeks` and `demo` profile routes both returned HTTP 200 HTML. Third-party API requests also returned HTTP 200 for `demo` and `geeksforgeeks`. |
| Metrics available | Third-party docs claim totals, difficulty, ratings, ranks, contests, and activity, but actual `geeksforgeeks` responses returned `totalSolved: 0`, `totalContests: 0`, null ratings/ranks and all-zero difficulty counts. The tested response does not substantiate the example metrics or prove their accuracy; metrics remain **UNKNOWN**, not verified real values. |
| Metrics unavailable | Official permitted automated source for solved totals, difficulty, rating, rank, contests, streaks, submissions, active days, and last activity: **UNKNOWN**. Do not interpret API zeros as proof of zero activity. |
| Request method | `GET` for public profile page; exploratory third-party `GET` only. No production request/client added. |
| Example request | `GET https://www.geeksforgeeks.org/user/geeksforgeeks/`; third-party probe: `GET https://gfg-stats.tashif.codes/geeksforgeeks/stats` |
| Example response shape | Official page probe: `200 text/html; charset=utf-8`. Third-party JSON: `{"status":"success","platform":"gfg","username":"geeksforgeeks","data":{"totalSolved":0,"totalQuestions":null,"acceptanceRate":null,"byDifficulty":{"school":0,"basic":0,"easy":0,"medium":0,"hard":0}}}`. This response is not accepted as verified data. |
| Rate-limit observations | No official public automation limit found. Third-party service did not establish an upstream limit or an authorization to retrieve GeeksforGeeks data. |
| Failure modes | Public route may return a generic/empty profile; third-party route returned HTTP 404 for an unknown handle; third-party may return apparent success with all-zero data. Timeout, rate-limit, malformed-response and stale-cache behavior are not independently documented/verified. |
| Terms/compliance concern | GeeksforGeeks Terms prohibit automated/non-human access and systematically retrieving data to build a collection without written permission. A third-party proxy does not resolve this concern when its authorization/upstream source is unknown. See [Terms of Use](https://www.geeksforgeeks.org/legal/terms-of-use/). |
| Reliability | Low for the third-party service: unknown operator guarantees/upstream, and observed values did not substantiate the documented example metrics. Official HTML extraction is not permitted by the reviewed terms. |
| Implementation decision | Keep `LIVE_SYNC_UNAVAILABLE`. Do not use the third-party service or parse GFG HTML unless GFG provides written permission/documentation and the source can be verified. |

### HackerRank

| Field | Finding |
|---|---|
| Platform | HackerRank |
| Source | Public profile pages: `https://www.hackerrank.com/profile/{username}`. Official API documentation covers enterprise workflow and team/user administration APIs, not anonymous public practice-profile statistics. |
| Source type | Public profile HTML; documented customer/enterprise APIs for unrelated workflows. |
| Official/Unofficial | Profile route and enterprise APIs are official; no public profile-stat API was found. |
| Authentication required? | Profile page probes did not require login. Documented Work APIs require an enterprise account and an API token. KodBTW will not request these credentials. |
| Publicly accessible? | Yes: ordinary GETs to `Gennady` and `sajal` profile routes returned HTTP 200 HTML. This only confirms page reachability. |
| Metrics available | **UNKNOWN** for permitted machine access. No public profile metrics were parsed. |
| Metrics unavailable | Public anonymous coding-profile stats API: **NOT_SUPPORTED** by the reviewed official API documentation. Specific metrics from authorized sources remain **UNKNOWN**. |
| Request method | `GET` profile-page probes only; no production acquisition call. |
| Example request | `GET https://www.hackerrank.com/profile/Gennady` |
| Example response shape | Probe: `200 text/html; charset=utf-8`; no data extracted. Official API overview describes token-authenticated APIs for tests, interviews, users, teams, and candidates rather than anonymous practice-profile stats. |
| Rate-limit observations | HackerRank’s API overview recommends up to 10 requests/second for its authenticated APIs; this does not authorize public-profile scraping. No public profile endpoint limit found. |
| Failure modes | Official profile FAQ says profiles without visible challenge/badge/certification activity may be unavailable/404. Other private, rate-limit, timeout, malformed, and server-error behavior was not exhaustively tested. |
| Terms/compliance concern | No public stats API terms were identified. HackerRank’s Terms restrict copying, distribution, public display, and using the service to develop/provide a competing product. See [Terms](https://www.hackerrank.com/about-us/terms-of-service); official [API overview](https://support.hackerrank.com/articles/2067417637-api-overview). |
| Reliability | Low for public HTML extraction; page availability does not establish a stable stats contract or reuse permission. |
| Implementation decision | Keep `LIVE_SYNC_UNAVAILABLE`. Use only if HackerRank publishes a suitable public API or grants explicit integration permission. |

## Existing LeetCode and Codeforces integrations

These integrations were not modified in this research pass.

| Platform | Source | Source type | Official/Unofficial | Auth | Publicly accessible | Metrics available | Metrics unavailable | Request method / example | Example response shape | Rate-limit observations | Failure modes | Terms/compliance concern | Reliability | Implementation decision |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| LeetCode | Existing `POST https://leetcode.com/graphql` query for `matchedUser` and `userContestRanking` | Undocumented public GraphQL endpoint | Unofficial/undocumented; LeetCode publishes no public contract for this query | No user credentials/cookies in existing client | Existing adapter uses it; no new live request made in this pass | Solved totals/difficulty counts, rating, ranking, contest attendance, current streak when returned | Max rating, submissions, active days, last activity, longest streak remain unsupported | Existing POST with JSON GraphQL payload | JSON GraphQL envelope with `data.matchedUser`; optional values null when absent | No published limit verified; 15-minute sync cooldown | Timeout, rate limit, not-found, malformed or changed schema | LeetCode Terms prohibit crawling/scraping/spidering; seek an approved API or permission before expanding. [Terms](https://leetcode.com/terms/) | Medium-low; schema is undocumented | Retain existing REAL adapter without expansion; terms compatibility remains a production risk |
| Codeforces | Official `GET https://codeforces.com/api/user.info?handles={handle}` | Documented public REST API | Official | No auth for public user information | Yes, official docs describe anonymous access | Current rating and max rating; adapter maps both separately | Solved totals/difficulty, numeric rank, contests participated, streak, activity are not provided by `user.info` | GET; e.g. `https://codeforces.com/api/user.info?handles=tourist` | `{\"status\":\"OK\",\"result\":[{\"handle\":\"tourist\",\"rating\":...,\"maxRating\":...,\"rank\":\"...\"}]}` | One request no more often than every two seconds; application serializes and spaces Codeforces calls. [Official docs](https://codeforces.com/apiHelp/?locale=ru&mobile=true) | Not found, rate limit, timeout, upstream error, malformed JSON | Official documented public API; do not infer numeric rank from rank title | High for documented fields | Retain REAL adapter; no submissions/history additions in this scope |

## Normalized metric status

Status describes KodBTW’s permitted, verified acquisition today. **UNKNOWN** is intentionally distinct from **UNAVAILABLE** and zero.

| Metric | CodeChef | GeeksforGeeks | HackerRank |
|---|---|---|---|
| `totalProblemsSolved` | UNKNOWN | UNKNOWN | UNKNOWN |
| `easySolved` | UNKNOWN | UNKNOWN | UNKNOWN |
| `mediumSolved` | UNKNOWN | UNKNOWN | UNKNOWN |
| `hardSolved` | UNKNOWN | UNKNOWN | UNKNOWN |
| `rating` | UNKNOWN | UNKNOWN | UNKNOWN |
| `maxRating` | UNKNOWN | UNKNOWN | UNKNOWN |
| `rank` | UNKNOWN | UNKNOWN | UNKNOWN |
| `contestsParticipated` | UNKNOWN | UNKNOWN | UNKNOWN |
| `currentStreak` | UNKNOWN | UNKNOWN | UNKNOWN |
| `longestStreak` | UNKNOWN | UNKNOWN | UNKNOWN |
| `submissions` | NOT_SUPPORTED | NOT_SUPPORTED | NOT_SUPPORTED |
| `activeDays` | UNKNOWN | UNKNOWN | UNKNOWN |
| `lastActivity` | UNKNOWN | UNKNOWN | UNKNOWN |
| `source` | `UNSYNCED` / `LIVE_SYNC_UNAVAILABLE` | `UNSYNCED` / `LIVE_SYNC_UNAVAILABLE` | `UNSYNCED` / `LIVE_SYNC_UNAVAILABLE` |
| `lastSyncedAt` | null until successful supported acquisition | null until successful supported acquisition | null until successful supported acquisition |

No missing metric is normalized to zero. Existing snapshots labelled `MOCK` remain identifiable as mock data and are not promoted to REAL. Failed syncs continue to preserve any prior successful snapshot.

## Probe and POC results

| Probe | Result |
|---|---|
| CodeChef public profile GETs | `lee215`: HTTP 200; `tourist`: no HTTP response in this environment. No page parsing performed. |
| GeeksforGeeks public profile GETs | `geeksforgeeks` and `demo`: HTTP 200. No profile parsing performed. |
| Third-party GFG stats API | `demo/profile`, `geeksforgeeks/profile`, `geeksforgeeks`, `/stats`, and `/contests` were reachable without auth. The public profile stats were all zero/null, and an unknown username returned 404. Those results do not prove actual stats correctness or source authorization. |
| HackerRank public profile GETs | `Gennady` and `sajal`: HTTP 200. No page parsing performed. |

No provider client/adapter POC was created for these platforms: extracting the stats from the official pages would conflict with the reviewed terms or lacks a stable, authorized API; the third-party GFG API failed the data-quality/provenance bar. Existing adapter tests continue to cover safe `LIVE_SYNC_UNAVAILABLE` responses and ensure unsupported adapters do not create fabricated snapshots. A compliant integration should only proceed after the platform documents a suitable API or grants written permission, followed by a real public-account POC and deterministic parser/client tests.

## Existing sync policy

- `PlatformAdapter` remains the provider boundary; the existing LeetCode and Codeforces integrations are unchanged by this research update.
- For all platforms, provider failures preserve prior successful metrics and timestamps; unsupported integrations return safe `LIVE_SYNC_UNAVAILABLE` status.
- The configurable 15-minute sync cooldown applies to manual and scheduled sync. Codeforces keeps its separate minimum two-second spacing.
- New public sources must define their supported metrics, terms, rate limits, failure mapping, and deterministic tests before being connected to the persistence lifecycle.
