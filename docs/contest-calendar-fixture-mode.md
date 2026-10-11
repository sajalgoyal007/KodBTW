# Local Contest Calendar fixture mode

The backend defaults to the real Codeforces contest provider. For local browser testing without external provider requests, explicitly select the fixture provider before starting Spring Boot:

```powershell
$env:CONTEST_CALENDAR_PROVIDER = "fixture"
$env:CONTEST_CALENDAR_FIXTURE_SCENARIO = "multiple"
$env:CONTEST_CALENDAR_CACHE_TTL = "PT10M"
```

Supported scenarios are `multiple`, `empty`, `unavailable`, and `stale`. Fixture events are named `Fixture: ...`, use source `LOCAL_FIXTURE`, and link to the official Codeforces contest listing. They are illustrative UI data, not real scheduled contests. The fixture provider never calls or falls back to the Codeforces provider; the Codeforces provider and client beans are not created in fixture mode.

To test stale fallback, select `stale`, set `CONTEST_CALENDAR_CACHE_TTL` to `PT2S`, start the backend, then open the authenticated `/contests` page. The first request returns fixture events. After two seconds, press Refresh; the next fixture refresh fails and the cached events remain marked stale with their first successful `fetchedAt` timestamp.

To return to the normal provider, unset `CONTEST_CALENDAR_PROVIDER` and `CONTEST_CALENDAR_FIXTURE_SCENARIO` in that PowerShell process (or set the provider to `codeforces`). `CONTEST_CALENDAR_PROVIDER` accepts only `codeforces` and `fixture`; invalid selections and invalid fixture scenarios fail application startup. Fixture mode is local configuration only and has no runtime-switching endpoint.
