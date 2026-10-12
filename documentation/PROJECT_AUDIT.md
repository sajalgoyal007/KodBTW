# KodBTW repository audit

**Audit scope:** the checked-out repository, including existing unstaged and untracked source files, on 12 October 2026. This is a source/configuration audit, not a production penetration test, external-provider authorization review, or browser-based UX certification.

## Evidence categories

| Category | Finding |
|---|---|
| Implemented code paths | JWT registration/login and protected requests; profile CRUD and public profile response; platform account lifecycle; dashboard/history/analytics services; leaderboard projections; contest calendar; frontend routes and theme. Their presence is verified from source. Automated test results are recorded separately after the validation commands are run. |
| Implemented with external limitations | LeetCode adapter calls an undocumented public GraphQL query; Codeforces adapter uses the public `user.info` endpoint; CodeChef uses an unofficial third-party stats service and source label `CODECHEF_THIRD_PARTY`. External reliability and permission are not established by code or tests. |
| Present but disabled/prototype | GFG and HackerRank client/adapter files exist in the current working tree, including untracked files. `Platform.hasLiveStatsSource()` excludes both, so normal sync exits before adapter lookup. They remain unavailable/pending in the application. Their upstream permission is unresolved. |
| Partially supported | Contest-calendar fixture mode is selectable by local configuration and returns explicitly illustrative fixture records. Some platform metrics are not available from the corresponding provider and remain null. |
| Planned/unconfirmed | Official or written-authorized GFG/HackerRank integrations, production clearance for the CodeChef provider, server-rendered route metadata, true unknown-route 404 handling, production operational SLAs, and academic report deliverables are not established by this checkout. |
| Missing from checkout | SRS, final academic report, research paper, literature review deliverable, weekly/daily reports, presentation, demo video, genuine application screenshots, root licence file, and confirmed team/module ownership records. |

## Documentation gaps found

- The prior `docs/ARCHITECTURE.md` described an early phase as current and listed platforms without differentiating runtime status.
- `docs/CODEFORCES_INTEGRATION.md` mapped Codeforces `maxRating` to `rank`; current code stores it separately as `maxRating` and leaves numeric rank null.
- No schema ER diagram, UML index, workflow diagrams, module contribution record, testing report, weekly index, or screenshot index existed.
- The application social image is not a screenshot. No app screenshots or academic deliverable files were found.
- Git history contains authorship records for only two names from the supplied five-member list. Commit metadata is insufficient to assign module responsibilities; all individual contribution details require team confirmation.

## Source-of-truth pointers

- Runtime source: `backend/src/main/java/com/kodbtw/`
- Schema: `backend/src/main/resources/db/migration/V1__initial_schema.sql` through `V7__separate_max_rating_from_rank.sql`
- Tests: `backend/src/test/` and `frontend/tests/`
- CI: `.github/workflows/`
- Existing external acquisition research and permission notes: `docs/platform-data-acquisition.md`
- Detailed test execution: [Testing and Validation](TESTING_AND_VALIDATION.md)

## Audit limitations

No production database was inspected, no live user account was created, and no external platform permission was requested or assumed. Current adapter unit tests may mock HTTP responses and do not independently verify provider uptime, actual profile contents, or data-use permission. The deployment diagram documents repository-supported packaging/configuration, not a confirmed hosting account or live deployment topology.
