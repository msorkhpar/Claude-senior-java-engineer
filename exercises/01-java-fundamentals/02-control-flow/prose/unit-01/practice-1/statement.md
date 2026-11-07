The payments client sends each request through `RetryPolicy`, which looks at the HTTP status of
every response and decides what to do next. The on-call log from last week has two complaints:
requests that the provider throttled were dropped instead of retried, and a misbehaving proxy that
answered with status `600` kept the client retrying.

The policy the team agreed on:

| Status | Action |
|---|---|
| `429` or `503` | `RETRY_LATER` (the provider says when) |
| `200` to `299` | `DONE` |
| any other `400` to `499` | `GIVE_UP` |
| any other `500` to `599` | `RETRY_NOW` |
| anything else | `UNEXPECTED` |

`attempts(...)` already drives a request from these decisions. Write `decide(int status)` in
`src/main/java/practice/RetryPolicy.java` so that it follows the table for every `int`. About ten
lines in total.
