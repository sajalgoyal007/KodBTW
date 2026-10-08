# Platform data acquisition

Research and ordinary unauthenticated HTTP probes performed **8 October 2026**. These probes checked public-page availability only. No credentials, cookies, browser automation, XHR reverse engineering, or access-control bypasses were used. A page returning HTTP 200 does not establish permission for automated extraction.

## Platform findings

### CodeChef

| Field | Finding |
|---|---|
| Platform | CodeChef |
| Official API availability | No current official public API documentation for anonymous user-profile statistics was located. The legacy `developers.codechef.com` portal could not be retrieved in this review. Historical CodeChef Discuss replies say API access required requesting/receiving access, and a CodeChef representative stated the website's internal API was for CodeChef's own use; treat this as historical context, not proof of current API terms. See [API access discussion](https://discuss.codechef.com/t/codechef-api/85506) and [website API clarification](https://discuss.codechef.com/t/codechef-api-not-working/40806). |
| Official source / integration | CodeChef's [`codechef-org/codechef-sample-app`](https://github.com/codechef-org/codechef-sample-app) demonstrates OAuth with an app client key/secret, user consent, and an API request for a contest problem. It does not document an anonymous public-profile statistics API or establish access to the profile metrics listed below. The sample is not a suitable source for this integration. |
| Public profile source | `https://www.codechef.com/users/{username}`; ordinary profile `GET`. The earlier public-page probe returned HTTP 200 HTML for `lee215`; `tourist` did not return an HTTP response in that environment. That probe did not parse profile content. No new extraction probe was made after reviewing the current terms. |
| Public/profile facts | CodeChef's official [FAQ](https://www.codechef.com/faq) describes the user's public profile and rating; its [rating pages](https://www.codechef.com/ratings/all) expose rating, global rank, and country rank in a human-facing listing. These are evidence that some information is publicly viewable, not authorization for automated extraction or evidence of a stable JSON contract. |
| Structured requests / response | No public JSON, JSON-LD, XHR, or REST profile-stat request was verified in this review. No such endpoint is documented as a public profile API by CodeChef. Do not reverse engineer or depend on website-internal routes. |
| Authentication | The public profile HTML was reachable without login in the earlier single-handle probe. The official OAuth sample requires application credentials and end-user consent. No unauthenticated profile-stat API was established. |
| Metrics | For KodBTW's permitted machine-readable source, all normalized metrics remain **UNKNOWN**: total/easy/medium/hard solved, rating/max rating, stars, numeric/global/country/institution rank, contests, submissions, rating history, activity, streak, and last activity. Public human-facing profile/rating pages visibly discuss or display some rating/rank information; do not map those metrics without an approved, stable data source. Difficulty breakdowns and solved totals were not verified. |
| Rate limits / failure behavior | No rate limits or API error semantics for an approved public-profile API were found. The earlier profile probe had one successful HTML response and one no-response result; not-found, private/no-activity, rate-limit, and schema-change behavior remain unverified. |
| Terms / production permission | Current [CodeChef Terms of Service](https://www.codechef.com/terms) prohibit use to “spider, crawl, or scrape” (Section 14) and prohibit copying/exploiting the service or access without prior written permission (Section 2). A KodBTW user-triggered or scheduled profile collection is automated extraction; a public browser-visible page does not grant permission. The reviewed terms do not establish permission for it. Obtain written authorization before any automated acquisition. |
| Third-party source investigated | [Tashif Khan's CodeChef Stats API repository](https://github.com/tashifkhan/codechef-stats-api) explicitly describes itself as scraping public profiles; its docs list its own `/profile/{handle}`, `/heatmap/{handle}`, and `/rating/{handle}` routes. It is operated by an individual maintainer, not CodeChef. The repo indicates upstream fetches/caching/rate limiting but provides no evidence of CodeChef authorization. Maintenance activity does not establish upstream stability or data accuracy. It is not safe as a KodBTW production dependency. |
| Technical feasibility | Public human-facing pages expose at least some rating/rank information, and third-party projects demonstrate scraping is technically attempted. Structured fields, consistency, complete metrics, and stable unauthenticated endpoint behavior were not verified. |
| Production decision | **`LIVE_SYNC_UNAVAILABLE`**. Do not implement a profile scraper, parse embedded/page data, or use third-party proxy APIs unless CodeChef documents an appropriate public API or grants written permission and its contract is verified. |

### GeeksforGeeks

| Field | Finding |
|---|---|
| Platform | GeeksforGeeks |
| Official profile behavior | The current public route is `https://www.geeksforgeeks.org/profile/{username}`. GFG's Terms say profiles are visible to other users. Publicly indexed profile pages show user-facing sections such as Overview, Coding Score, and Posts. The prior repository probe tested legacy `/user/{username}/` pages and observed HTTP 200 HTML only; it did not extract profile metrics. No live profile or internal API request was made in this phase because the current terms prohibit automated access/extraction. |
| Official API availability | No official, documented public API for third-party retrieval of public profile statistics was found in current official documentation/search. The current profile-page JSON requests listed by the third-party project below are not official API documentation and are not a stable public contract. |
| Candidate upstream endpoints (third-party documentation only) | The third-party maintainer currently documents `GET https://authapi.geeksforgeeks.org/api-get/user-profile-info/?handle={username}&article_count=false&redirect=true`, `POST https://practiceapi.geeksforgeeks.org/api/v1/user/problems/submissions/` with a handle/year/month body, and `GET https://practiceapi.geeksforgeeks.org/api/v1/problems/{slug}/`. These are described by that project as GFG's current JSON endpoints that a browser calls; GFG does not publish them as a public stats API. They were not requested or independently verified in this phase. |
| Third-party API | [`gfg-stats.tashif.codes`](https://github.com/tashifkhan/GFG-Stats-API), maintained by the GitHub account `tashifkhan`, is unofficial and unaffiliated with GFG. Its documented unauthenticated routes include `GET /{username}`, `GET /{username}/profile`, and activity/contest endpoints; examples expose total and difficulty counts, coding score, institute rank, streak, contest/rating/activity data. It advertises response caching and rate limiting. Its current documentation says it uses the upstream JSON endpoints while impersonating a browser, and no GFG authorization is established. This proxy does not resolve the terms issue and is not an acceptable production dependency. |
| Previous third-party observations | Earlier repository probes found the hosted API reachable without authentication. The `geeksforgeeks` result contained zero solved/contest values and null rating/rank fields; an unknown handle returned HTTP 404. Those results do not verify the source's accuracy or permission and zero is not treated as proof of no activity. No new third-party request was made in this phase. |
| Actual normalized metrics | No metrics were successfully obtained from a verified, permitted source in this phase. Total solved; school/basic/easy/medium/hard counts; coding score/rating/max rating; institute/global rank; contest participation; streak; submissions; activity calendar/heatmap; and last activity remain **UNKNOWN** for KodBTW acquisition. Third-party claims/examples are not validated data. |
| Authentication / anti-bot | The user-facing profile is publicly viewable per GFG's terms and normal pages/indexing, but that does not authorize automated collection. No official unauthenticated third-party stats API was identified. The third-party service says it mimics browser requests to internal JSON APIs; this is not adopted or tested. No cookies, credentials, CAPTCHA, anti-bot, or access-control bypass was used. |
| Rate limits / reliability | No official automation limit or public API service-level contract was found. The third-party API claims rate limiting and caching, but no upstream quota, freshness, or availability guarantee was established. Prior API results included apparent success with all-zero/null statistics; the endpoint's semantics, schema stability, privacy/no-activity behavior, and error handling are not verified. |
| Current Terms / permission | GFG's current [Terms of Use](https://www.geeksforgeeks.org/legal/terms-of-use/) (page states last updated 2 June 2023) say users will not access Services through automated or non-human means (Section 1); prohibit systematically retrieving data to create or compile a collection/database without written permission; prohibit automated data gathering/extraction tools and scrapers except standard search-engine/browser usage; and prohibit bypassing access-restriction measures (Section 9). GFG also states profiles are visible to other users, but visibility does not override these restrictions. Automated KodBTW sync therefore requires explicit written GFG permission or a documented API authorization that covers this use. |
| POC / implementation decision | **No live POC was run** because the reviewed terms prohibit the automated source acquisition required by the proposed adapter. Do not implement a scraper, call the internal JSON routes, or consume the proxy. Keep `LIVE_SYNC_UNAVAILABLE` until GFG provides written permission or documents a suitable third-party API and its permitted use. |

### HackerRank

| Field | Finding |
|---|---|
| Platform | HackerRank |
| Official public profile behavior | Profile route: `https://www.hackerrank.com/profile/{username}`. HackerRank's current Profile FAQ says profiles with no visible activity (completed challenge, badge, or public certification) return 404, and username changes invalidate the old route. Official scoring docs say badges are visible to other users and describe challenge points, practice-track leaderboards, domain rating, and rated contests. ([Profile FAQ](https://help.hackerrank.com/articles/4472358331-profile-and-preferences-faqs), [Scoring](https://www.hackerrank.com/scoring)) |
| Official API availability | Current official [API Overview](https://support.hackerrank.com/articles/2067417637-api-overview) documents APIs for Work workflows: tests/candidates/results, interviews, user/team administration, membership, and questions. No anonymous public community-profile stats API is listed. The linked official API docs are HackerRank for Work at `https://www.hackerrank.com/work/apidocs`; they are not documented as third-party access to public practice profiles. |
| API authentication / audience | The API Overview says each enterprise user with a HackerRank for Work account can generate an API token after logging in. It is a customer workflow API, not anonymous public-profile access. No OAuth-based public stats API was found. KodBTW will not use enterprise/customer tokens. |
| Official machine-readable endpoints | No public profile-stat endpoint or schema was found in official docs. The documented API catalog names Work APIs but does not provide an authorized anonymous stats endpoint. No internal profile routes were probed in this phase. |
| Officially described visible information | Official scoring/FAQ material supports that profiles may expose earned badges, challenge activity/certifications, practice points and track leaderboards, domain ratings, and rated-contest details. It does not establish anonymous machine retrieval or availability of every candidate field. |
| Third-party candidate | [`tashifkhan/hackerrank-stats-api`](https://github.com/tashifkhan/hackerrank-stats-api), hosted at `hackerrank-stats.tashif.codes`, is an unofficial FastAPI service. Its README documents unauthenticated JSON routes `GET /{username}`, `GET /{username}/profile`, `GET /{username}/heatmap`, `GET /{username}/badges`, and `GET /{username}/contests`; it claims solved count, practice score, best active practice-track rank, reputation/profile level, badges, recent submissions, per-track stats, activity/streaks, and contest history/rating/global rank/percentile. Its documentation explicitly says difficulty breakdowns and platform-wide totals are not available; it warns that public submission history can be empty even when activity exists. |
| Third-party upstream / reliability | The README attributes results to public HackerRank endpoints but does not identify exact upstream URLs or document their stability/authorization. No independent upstream request or hosted API call was made. GitHub showed 37 commits and the current README was indexed within days of this review; this does not establish support guarantees, data correctness, or permission. README does not establish an API key requirement, service-level agreement, cache freshness, or rate limit. It does not describe CAPTCHA/cookie use; underlying request behavior was not independently inspected or tested. |
| Rate limits | HackerRank's official API Overview recommends up to 10 requests/second and documents 429 behavior for its authenticated Work APIs. This limit does not apply as authorization or a published quota for public-profile routes. No official public-profile rate limit was found. |
| Current Terms / permission | Current [HackerRank Terms of Service](https://www.hackerrank.com/about-us/terms-of-service) Section 6.1 prohibits circumventing security/access controls/use limits, copying/distributing/publicly displaying/modifying/reverse engineering Services, using Services for competitive analysis or to develop/provide a competing product, and combining Services with third-party products/services except as authorized by HackerRank. The terms do not provide an authorization for KodBTW to aggregate public-profile stats. Applicability of the competing-service clause to KodBTW is not determined here; the third-party proxy does not grant HackerRank permission. Obtain written authorization before integration. |
| POC / field status | No live POC was run because an official authorized source was not established and using the public-facing/undocumented routes as part of a third-party aggregation service raises explicit Terms concerns. Earlier repository probes of profile HTML returned HTTP 200 for `Gennady` and `sajal` without parsing; these establish page reachability only. For a permitted KodBTW source, total/easy/medium/hard solved, rating/max rating, rank, contests, streaks, badges, submissions/activity, and last activity all remain **UNKNOWN**. |
| Implementation decision | Keep `LIVE_SYNC_UNAVAILABLE`. Do not use public HTML, undocumented internal routes, or the unofficial proxy until HackerRank documents and authorizes a suitable public API or grants explicit written permission for this use. |

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
| `submissions` | UNKNOWN | NOT_SUPPORTED | NOT_SUPPORTED |
| `activeDays` | UNKNOWN | UNKNOWN | UNKNOWN |
| `lastActivity` | UNKNOWN | UNKNOWN | UNKNOWN |
| `source` | `UNSYNCED` / `LIVE_SYNC_UNAVAILABLE` | `UNSYNCED` / `LIVE_SYNC_UNAVAILABLE` | `UNSYNCED` / `LIVE_SYNC_UNAVAILABLE` |
| `lastSyncedAt` | null until successful supported acquisition | null until successful supported acquisition | null until successful supported acquisition |

No missing metric is normalized to zero. Existing snapshots labelled `MOCK` remain identifiable as mock data and are not promoted to REAL. Failed syncs continue to preserve any prior successful snapshot.

## Probe and POC results

| Probe | Result |
|---|---|
| CodeChef public profile GETs | Earlier probe: `lee215` returned HTTP 200; `tourist` returned no HTTP response. No page parsing was performed. Current terms were reviewed before any new endpoint/field probe; no new extraction request was made. |
| GeeksforGeeks public profile GETs | `geeksforgeeks` and `demo`: HTTP 200. No profile parsing performed. |
| Third-party GFG stats API | `demo/profile`, `geeksforgeeks/profile`, `geeksforgeeks`, `/stats`, and `/contests` were reachable without auth. The public profile stats were all zero/null, and an unknown username returned 404. Those results do not prove actual stats correctness or source authorization. |
| HackerRank public profile GETs | `Gennady` and `sajal`: HTTP 200. No page parsing performed. |

No provider client/adapter POC was created for these platforms: extracting the stats from the official pages would conflict with the reviewed terms or lacks a stable, authorized API; the third-party GFG API failed the data-quality/provenance bar. Existing adapter tests continue to cover safe `LIVE_SYNC_UNAVAILABLE` responses and ensure unsupported adapters do not create fabricated snapshots. A compliant integration should only proceed after the platform documents a suitable API or grants written permission, followed by a real public-account POC and deterministic parser/client tests.

## Existing sync policy

- `PlatformAdapter` remains the provider boundary; the existing LeetCode and Codeforces integrations are unchanged by this research update.
- For all platforms, provider failures preserve prior successful metrics and timestamps; unsupported integrations return safe `LIVE_SYNC_UNAVAILABLE` status.
- The configurable 15-minute sync cooldown applies to manual and scheduled sync. Codeforces keeps its separate minimum two-second spacing.
- New public sources must define their supported metrics, terms, rate limits, failure mapping, and deterministic tests before being connected to the persistence lifecycle.

## Phase 13D — Third-Party Provider POC (2026-10-08)

### 1. Provider investigated

Reviewed the hosted CodeChef, GeeksforGeeks, and HackerRank statistics services maintained by `tashifkhan` at `*.tashif.codes`, their endpoint documentation, upstream descriptions, and source repositories. **No live statistics endpoint was called in this phase.** Provider documentation describes upstream access that lacks established permission, so a live probe would exercise an unapproved acquisition path. Historical GFG results above are not Phase 13D observations.

### 2. CodeChef

#### Endpoint

The provider advertises unauthenticated `GET /{handle}`, `/{handle}/profile`, `/{handle}/stats`, `/{handle}/contests`, `/{handle}/rating`, `/{handle}/heatmap`, `/{handle}/badges`, and `/{handle}/topics` at `https://codechef-stats.tashif.codes`. It documents upstream scraping of `https://www.codechef.com/users/{handle}` and additional practice/recent-user routes. These are provider routes, not CodeChef API documentation. [Endpoints](https://tashif.codes/docs/codechef-stats-api/3-canonical-endpoints) · [Request path](https://tashif.codes/docs/codechef-stats-api/5.2-request-path)

#### Authentication

Provider claims its routes need no authentication. No credentials or token were used.

#### Response

Provider docs describe JSON envelopes, a 404 for a missing profile, and cases where upstream misses may yield error/empty-data responses. Not independently verified. [Schemas](https://tashif.codes/docs/codechef-stats-api/3.1-envelope-and-schemas)

#### Fields

Provider claims include total solved, current/highest rating, stars, rating history/rank, heatmap/activity, and topics. Badge data may be empty. No fields were observed live in this phase.

#### Data quality

Stats are documented as parsed from the public profile page; topic data uses additional practice endpoints. Response consistency and private/no-activity semantics remain unverified.

#### Reliability

Provider claims caching, backoff, and rate limits, but states these protections depend on Redis configuration. No independently verified SLA, schema guarantee, or freshness commitment was found. [Overview](https://tashif.codes/docs/codechef-stats-api/1-overview)

#### Provider terms/license

The [repository](https://github.com/tashifkhan/codechef-stats-api) shows no applicable license. No hosted API terms, KodBTW/student/commercial authorization, attribution policy, or SLA was found. Its upstream is a scraper; [CodeChef Terms](https://www.codechef.com/terms) prohibit crawling/scraping and copying/exploiting service access without prior written permission (see platform findings above).

#### Production suitability

Unauthenticated access does not establish permission. The proxy outsources the restricted upstream acquisition; permission and operational guarantees remain unresolved.

#### Decision

**RED — DO NOT USE.** No live POC or production adapter.

### 3. GeeksforGeeks

#### Endpoint

The provider advertises unauthenticated `GET /{username}`, `/profile`, `/stats`, `/contests`, `/rating`, `/heatmap`, `/badges`, and `/topics` at `https://gfg-stats.tashif.codes`. Documented upstreams include `GET https://authapi.geeksforgeeks.org/api-get/user-profile-info/` and practice API submissions/problem routes. GFG does not document these as a public third-party stats API. [Endpoints](https://tashif.codes/docs/gfg-stats-api/3-canonical-endpoints) · [Upstream JSON](https://tashif.codes/docs/gfg-stats-api/5.1-upstream-json)

#### Authentication

Provider says its routes need no authentication. No credential or token was used.

#### Response

Provider documents profile/statistics/contest/rating/activity envelopes and errors for missing profiles, failed submissions, timeouts, and connection errors. No response was requested in this phase. [Schemas](https://tashif.codes/docs/gfg-stats-api/3.1-envelope-and-schemas) · [Request path](https://tashif.codes/docs/gfg-stats-api/5.2-request-path)

#### Fields

Claims include coding score, total/difficulty solved, institute rank, streak, and heatmap/activity. Detailed notes say contest/rating/badge models may be empty or hardcoded zero/null, conflicting with examples that show populated values. No field is validated for KodBTW.

#### Data quality

The provider says heatmap is synthesized from per-problem timestamps, not a native GFG calendar. Its summary examples materially conflict with detailed platform notes about unsupported/empty fields. Historical hosted response and 404 probes above are not repeated or treated as proof of accuracy.

#### Reliability

Provider claims Redis-backed caching and per-IP/per-handle limits, but says these are absent without Redis. No uptime, freshness, schema, or service-level guarantee was found.

#### Provider terms/license

The provider source declares [MIT](https://github.com/tashifkhan/GFG-Stats-API/blob/main/LICENSE), which licenses code and does not grant hosted-service or upstream data rights. No hosted API terms or KodBTW/student/commercial permission was found. The provider says it mimics browser requests to GFG internal JSON APIs, contrary to restrictions in [GFG Terms](https://www.geeksforgeeks.org/legal/terms-of-use/) on automated/non-human access and automated extraction/systematic retrieval without permission (see GFG findings above).

#### Production suitability

The upstream method raises a direct terms concern; schema inconsistencies further lower confidence. The repository license does not authorize upstream data access.

#### Decision

**RED — DO NOT USE.** No live POC or production adapter.

### 4. HackerRank

#### Endpoint

The provider advertises unauthenticated `GET /{username}`, `/profile`, `/stats`, `/contests`, `/rating`, `/heatmap`, `/badges`, and `/topics` at `https://hackerrank-stats.tashif.codes`. It says the service calls HackerRank `/rest` routes including `/rest/contests/master/hackers/{username}/profile`, `/rest/hackers/{username}/scores_elo`, badge, contest participation, rating history, submission history, and recent challenge routes. These are undocumented internal routes, not an official public stats API. [Endpoints](https://tashif.codes/docs/hackerrank-stats-api/3-canonical-endpoints) · [Upstream routes](https://tashif.codes/docs/hackerrank-stats-api/5.1-public-rest) · [Request path](https://tashif.codes/docs/hackerrank-stats-api/5.2-request-path)

#### Authentication

Provider claims its routes and upstream calls are unauthenticated/logged-out. HackerRank's documented Work API requires an enterprise user token and does not provide anonymous community-profile stats. No token, credentials, or cookies were used. [Official API overview](https://support.hackerrank.com/articles/2067417637-api-overview)

#### Response

Provider claims profile/stats/contest/badge/activity JSON. It documents missing profiles as 404 but tolerates some other upstream 404s, potentially returning partial successful cards. Not independently tested. [Schemas](https://tashif.codes/docs/hackerrank-stats-api/3.1-envelope-and-schemas)

#### Fields

Claims include solved totals, practice score, best active track rank, badges, recent submissions, track stats, contest history/rating/global rank/percentile, and heatmap. Provider says difficulty breakdown/platform-wide totals are unavailable and activity history may be empty despite visible activity. No fields were observed live.

#### Data quality

The provider calls itself a typed proxy over public REST routes. Those routes have no public contract; tolerated missing subroutes can make incomplete results appear successful. Schema semantics and stability remain unverified.

#### Reliability

Provider claims a 20-second upstream timeout plus Redis-dependent cache/rate limits. No SLA, freshness guarantee, authorized quota, or durable schema contract was found.

#### Provider terms/license

The [repository](https://github.com/tashifkhan/hackerrank-stats-api) shows no applicable license. No hosted API terms, attribution, or KodBTW/student/commercial authorization was found. [HackerRank Terms](https://www.hackerrank.com/about-us/terms-of-service) restrict copying/public display and combining Services with third-party services except as authorized (see findings above). Proxying undocumented routes does not settle permission.

#### Production suitability

This is an unaffiliated proxy over undocumented upstream routes. Public reachability establishes neither authorization nor data rights, schema stability, or reliable operations.

#### Decision

**RED — DO NOT USE.** No live POC or production adapter.

### 5. PlatformStats mapping

This is a future mapping guide, not observed or permitted data. No provider passed the permission gate, so do not implement these mappings or emit `*_THIRD_PARTY` source labels. Map only present, valid fields with verified equivalent semantics; otherwise keep null. Never turn absent difficulty metrics into zero.

| Candidate provider field | `PlatformStats` mapping if later authorized | Current status |
|---|---|---|
| Explicit total solved | `totalProblemsSolved` | Unverified; null |
| Explicit difficulty counts | `easySolved`, `mediumSolved`, `hardSolved` | Unverified; null if absent |
| Current / highest rating | `rating` / `maxRating` | Claims only; null |
| Explicit numeric rank | `rank` | Semantics unverified; null |
| Contest count | `contestsParticipated` | GFG examples conflict; null |
| Explicit current / longest streak | `currentStreak` / `longestStreak` | Claims only; null |
| Last activity timestamp | No current direct field | Not mappable without model change and permission |
| Badges, submissions, heatmap, topics | No direct normalized fields | Not mappable; do not synthesize metrics |
| Source / sync time | `source` / `lastSyncedAt` | Keep unavailable status; no third-party label |

### 6. Architecture changes

None. No client, adapter, registry, sync service, persistence, schema, frontend, or database changes. Existing `PlatformAdapter`, 15-minute sync cooldown, and failure persistence already keep unsupported sources unavailable and preserve prior snapshots after a failed sync.

### 7. Tests

No adapter or HTTP mock tests were added because implementation was rejected at the permission gate. No live external POC calls were made. Requested existing-project checks are recorded after execution below.

### 8. Security review

- No enterprise token, password, session cookie, CAPTCHA/Cloudflare bypass, browser automation, Codolio API, or third-party API call was used in this phase.
- Provider claims that hosted endpoints are unauthenticated establish reachability only, not authorization for KodBTW.
- CodeChef/GFG docs describe scraping or browser impersonation; HackerRank describes a proxy over undocumented `/rest` routes. No upstream authorization was found.
- GFG's MIT license covers repository code, not data use. No hosted-provider terms, explicit student/commercial permission, attribution rules, or availability commitments were found.
- Calling these hosts would transmit users' platform handles without a verified data-handling agreement.

### 9. Final decision

**DO NOT USE any of the three providers.** All are RED for production. No source meets both permission and reliability requirements. Preserve `LIVE_SYNC_UNAVAILABLE` for CodeChef, GFG, and HackerRank. Do not label data `*_THIRD_PARTY` or `*_REAL`. Reconsider only after platform authorization and provider terms establish permitted use, data handling, stability, and operations.

### 10. Deployment recommendation

Do not deploy an integration from this phase. No runtime code changed and no migration is needed. Keep the existing documentation edits and this review available for review; do not commit automatically.

### Verification record

- `backend: .\mvnw.cmd test` — BUILD SUCCESS; 185 tests, 0 failures, 0 errors, 2 skipped.
- `backend: .\mvnw.cmd -DskipTests package` — BUILD SUCCESS.
- `frontend: npm run build` — BUILD SUCCESS (`tsc -b && vite build`).
- `git diff --check` — passed.
- E2E — not run; no integration or user-facing behavior was implemented, and no E2E command was specified for this documentation-only decision.

## Platform Authorization Status

This section is the implementation gate for CodeChef, GeeksforGeeks, and HackerRank. Keep each connected account available in the five-platform connection layer, but keep live statistics unavailable until the evidence listed below has been reviewed. A public page or technically reachable endpoint is not sufficient. Do not set a `*_REAL` source based only on an API response.

| Platform | Current status | Required authorization | Exact data requested | Expected source/API contract | Evidence required before enabling `REAL` | Post-approval implementation files |
|---|---|---|---|---|---|---|
| CodeChef | Connectable; `SOURCE_PENDING` / `LIVE_SYNC_UNAVAILABLE` | Current written approval and API access from CodeChef for KodBTW's automated retrieval, aggregation, dashboard/public-profile display, and snapshot storage. Confirm this is permitted for a student/non-commercial third-party product. | Public handle/profile URL; total solved; easy/medium/hard counts if officially supported; current and highest rating; rank with explicit scope; contest count; current/longest streak only if authoritative; source timestamp. Leave unsupported values null. | Official, documented HTTPS API or written-approved endpoint; documented auth/scopes, response schema, error semantics, quota, cache/retention and attribution rules. No website-internal routes. | Written permission or current official developer terms covering this use; approved API docs and credentials flow; successful contract validation for multiple public profiles and not-found/no-activity cases; rate-limit and caching requirements recorded; provenance/version recorded. | `backend/.../adapter/impl/CodeChefAdapter.java`; new `backend/.../adapter/codechef/CodeChefClient.java` and response DTOs/tests; `backend/.../entity/Platform.java`; sync/persistence mapping only if contract requires it; DTO/entity/repository/frontend and migration only for approved fields absent from current schema. |
| GeeksforGeeks | Connectable; `SOURCE_PENDING` / `LIVE_SYNC_UNAVAILABLE` | Explicit written GFG permission for automated retrieval of public profile statistics, aggregation, dashboard/public-profile display, and snapshot storage; clarify student/non-commercial eligibility. | Public handle/profile URL; total solved; easy/medium/hard counts when defined; coding score if GFG authorizes it and product mapping is agreed; rank only with scope; contests/streak/activity only if directly supported and permitted. Do not infer school/basic counts as difficulty counts. | Official documented public profile API or GFG-approved endpoint; schema, auth, quota, errors, cache/retention and attribution defined. No undocumented internal JSON endpoints. | Written authorization specifically covering scheduled/manual automated retrieval and redistribution/display; official or approved contract; multiple profile fixtures validated including private/no-activity and missing fields; field semantics and null behavior agreed; quotas and retention documented. | `backend/.../adapter/impl/GeeksForGeeksAdapter.java`; new `backend/.../adapter/geeksforgeeks/GeeksForGeeksClient.java` and response DTOs/tests; `backend/.../entity/Platform.java`; sync/persistence mapping only if needed; DTO/entity/repository/frontend and migration only for authorized fields not represented today. |
| HackerRank | Connectable; `SOURCE_PENDING` / `LIVE_SYNC_UNAVAILABLE` | Keep pending unless HackerRank provides an authorized community-profile API or grants explicit written permission for the requested automated acquisition and display. Do not use HackerRank for Work enterprise tokens for community stats. | Public handle/profile URL; total solved; practice score only as its own metric (not rating); difficulty counts only if supported; contest rating/history/count with explicit semantics; rank with scope; badges/activity/streak only if authorized and complete enough. | Official documented community API or written-approved endpoint; contract must distinguish practice track score/rank from contest rating/rank; documented authentication, quotas, error behavior, attribution, cache/retention. | Current official documentation or written authorization for community profile data and aggregation/display; verified contract/schema and profile edge cases; separate metric definitions and permissions; quota, caching, and retention rules recorded. | `backend/.../adapter/impl/HackerRankAdapter.java`; new `backend/.../adapter/hackerrank/HackerRankClient.java` and response DTOs/tests; `backend/.../entity/Platform.java`; sync/persistence mapping only if needed; DTO/entity/repository/frontend and migration only for authorized fields not represented today. |

### Permission request drafts — not sent

#### CodeChef

**Subject:** Permission and API access request for KodBTW public profile statistics

Hello CodeChef team,

I am building KodBTW, a student/non-commercial platform that lets users connect coding profiles and view their coding statistics in one place. We would like to ask for permission to retrieve public CodeChef profile statistics automatically, aggregate them in KodBTW, and display them on a user's private dashboard and, when that user enables it, their public KodBTW profile.

Could you confirm whether this use is permitted and whether student/non-commercial use is allowed? If available, please share the official developer/API documentation and the supported profile-statistics endpoints, required application/user authentication and scopes, rate limits, and error behavior. Please also specify attribution requirements, permitted caching and historical snapshot retention, and any restrictions on redistribution or display of the statistics.

We will not scrape profile pages or use undocumented website-internal endpoints. We will keep CodeChef statistics unavailable until we have your authorization and follow the approved API contract.

Thank you,
KodBTW team

#### GeeksforGeeks

**Subject:** Written permission request for automated public profile statistics in KodBTW

Hello GeeksforGeeks team,

I am building KodBTW, a student/non-commercial platform that lets users connect coding profiles and view their coding statistics in one place. We request written permission to retrieve public GeeksforGeeks profile statistics automatically, aggregate them in KodBTW, and display them on a user's private dashboard and, when that user enables it, their public KodBTW profile.

Could you confirm whether this use is permitted for a student/non-commercial project? If an official public profile API or approved endpoint exists, please share its documentation, authentication requirements, supported statistics and field definitions, rate limits, and error behavior. Please also specify attribution requirements, allowed caching and snapshot retention periods, and any redistribution or display restrictions.

We will not scrape profile pages, call undocumented internal JSON endpoints, or bypass access controls. GeeksforGeeks statistics will remain unavailable until we receive written authorization and an approved source contract.

Thank you,
KodBTW team

#### HackerRank

**Subject:** Authorization and API documentation request for HackerRank community profile statistics

Hello HackerRank team,

I am building KodBTW, a student/non-commercial platform that lets users connect coding profiles and view their coding statistics in one place. We would like to ask whether HackerRank authorizes KodBTW to retrieve public community-profile statistics, aggregate them, and display them on a user's private dashboard and, when that user enables it, their public KodBTW profile.

Could you confirm whether this is permitted for a student/non-commercial project and provide documentation for any authorized community-profile API or endpoint? Please specify supported metrics and their definitions (including practice score versus contest rating, and track rank versus global rank), authentication requirements, rate limits, and error behavior. Please also clarify attribution requirements, allowed caching and snapshot retention, and any redistribution or display restrictions.

We will not use enterprise Work API tokens for community profile data and will not call undocumented routes or bypass access controls. HackerRank statistics will remain unavailable unless an authorized source is documented or explicit written permission is granted.

Thank you,
KodBTW team

### Permission-ready verification

- Five platforms remain in the existing account model; the three pending platforms are connectable and are not live sync sources.
- Manual and scheduled sync share `PlatformSyncService`; it returns before adapter lookup/fetch for platforms where `Platform.hasLiveStatsSource()` is false. Dashboard/analytics/public-profile reads use `PlatformStatsService`, which rejects snapshots without a non-MOCK `*_REAL` source. Leaderboard rebuild reads the same filtered stats and does not fetch a pending platform; leaderboard scheduling's sync phase shares the same early return. History and insights select/filter `*_REAL` snapshots.
- No MOCK snapshot is returned as current stats or consumed as real dashboard/analytics/history/public-profile data. Leaderboard score aggregation is real-only. MOCK-labelled data may still be recognized internally as mock metadata, but it is not presented as verified stats.
- Existing LeetCode and Codeforces adapter/client code was not changed. This review does not certify their upstream terms beyond the research findings above.
- No external permission requests were sent.
- Verification commands for this phase are recorded below.

### Phase 15B verification record

- `backend: .\mvnw.cmd test` — BUILD SUCCESS; 188 tests, 0 failures, 0 errors, 2 skipped.
- `backend: .\mvnw.cmd -DskipTests package` — BUILD SUCCESS.
- `frontend: npm run build` — BUILD SUCCESS (`tsc -b && vite build`).
- `git diff --check` — passed (Git emitted only its configured LF-to-CRLF working-copy warning).
- E2E — not run; this phase changed documentation only and did not specify an E2E command.
- No production DB access, migration, external permission request, or commit was performed.
