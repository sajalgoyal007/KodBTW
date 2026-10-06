# Codeforces Integration (Phase 4B-2B)

## 1. API Endpoint
- **Endpoint**: `GET https://codeforces.com/api/user.info?handles={handle}`
- **Documentation**: Official Codeforces Public API (`https://codeforces.com/apiHelp/methods#user.info`)

## 2. API Status & Authentication
- **Status**: Official, publicly supported REST API.
- **Authentication**: None required. Operates completely without API keys, credentials, cookies, or tokens.
- **Zero Credential Storage**: No passwords, cookies, session tokens, or private user data are ever accepted or stored.

## 3. Data Field Mapping
| Codeforces Field | PlatformStats Field | Notes |
| :--- | :--- | :--- |
| `result[0].handle` | `username` | Sanitized public handle |
| `result[0].rating` | `rating` | Current contest rating (null if unrated) |
| `result[0].maxRating` | `rank` | Highest historical rating achieved |
| Custom or Fallback | `profileUrl` | Preserves custom URL if provided; otherwise `https://codeforces.com/profile/{handle}` |
| System Timestamp | `lastSyncedAt` | Current UTC timestamp when fetched |
| Static Identifier | `source` | Set to `"CODEFORCES_REAL"` |

## 4. Unavailable Metrics
The public `user.info` endpoint does not provide problem-solving counts or contest streak information. To maintain data integrity without fabrication, the following fields are returned as `null`:
- `totalProblemsSolved`
- `easySolved`
- `mediumSolved`
- `hardSolved`
- `contestsParticipated`
- `currentStreak`
- `longestStreak`

## 5. Rate Limiting & Error Handling
- **Rate Limit Policy**: Codeforces allows up to 5 requests per second per IP across the public API. KodBTW issues single on-demand queries with no aggressive retry loops.
- **Error Mapping**:
  - **HTTP 400 / Status `FAILED`**: User not found returns `404 Not Found` (`ResourceNotFoundException`).
  - **Rate Limit ("Call limit exceeded")**: Throws `PlatformApiException` mapping to `502 Bad Gateway`.
  - **HTTP 5xx / Network Timeout**: Configured with a 5-second connection/read timeout, throwing `PlatformApiException` mapping to `502 Bad Gateway`.
  - **No Mock Fallback**: If an error occurs, upstream failures are returned cleanly without falling back to mock data.
