# Codeforces profile integration

This note follows the current `CodeforcesAdapter`, `CodeforcesClientImpl`, and `PlatformStats` model.

## Endpoint and authentication

- Profile statistics: `GET https://codeforces.com/api/user.info?handles={handle}` (public Codeforces API).
- KodBTW sends the public handle only; no Codeforces account password, cookie, or session token is collected.
- Contest-calendar data is provided separately by the Codeforces contest provider.

## Field mapping

| Codeforces response | KodBTW field | Notes |
|---|---|---|
| `handle` | `username` | Requested public handle |
| `rating` | `rating` | Current contest rating; nullable for an unrated user |
| `maxRating` | `maxRating` | Highest historical rating; stored separately from numeric rank |
| `rank` | Not mapped to numeric `rank` | Codeforces `rank` is a title string (for example, `expert`), not a numeric global rank |
| Custom account URL or generated profile URL | `profileUrl` | Defaults to `https://codeforces.com/profile/{handle}` |
| Fetch time | `lastSyncedAt` | Set when the response is mapped |
| Adapter source | `source` | `CODEFORCES_REAL` |

`GET user.info` does not provide solved-problem totals, difficulty counts, contest attendance totals, or streaks used by this model. Those fields remain `null`; no value is inferred as zero. The current code also does not map Codeforces max rating into `rank`.

## Sync and failures

`PlatformSyncService` applies a two-second spacing between Codeforces sync calls and the configured per-account sync cooldown. The client configures five-second connect/read timeouts and maps not-found responses separately from upstream/network failures. It does not implement an aggressive retry loop or fallback to mock statistics. Failed sync status is persisted while the previous successful snapshot is retained.

Upstream behavior and API terms can change; adapter/client tests validate controlled responses, not live service reliability. For the wider source/permission review, see [`platform-data-acquisition.md`](platform-data-acquisition.md).
