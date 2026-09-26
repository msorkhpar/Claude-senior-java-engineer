Java 11's HTTP client (`java.net.http`) is built with fluent builders:
`HttpClient.newBuilder()` and `HttpRequest.newBuilder()`. The page's pitfall
is a client with no timeout, which can hang indefinitely. Its fix needs **two**
timeouts: a **connect timeout** on the client, and a **per-request timeout**,
because `connectTimeout` does not cover waiting for the response.

Nothing here is sent: you only build the objects, and the tests inspect them.

Write the class `Requests`:

- `client()` returns an `HttpClient` that prefers HTTP/2, follows redirects with
  `HttpClient.Redirect.NORMAL`, and has a connect timeout of 10 seconds.
- `postJson(String url, String json)` returns an `HttpRequest` that POSTs `json`
  to `url` with the header `Content-Type: application/json` and a timeout of
  30 seconds.

| call | what the tests read back |
|---|---|
| `client()` | `version()` is `HTTP_2`, `followRedirects()` is `NORMAL`, `connectTimeout()` is `Optional[PT10S]` |
| `postJson("https://api.example.org/items", "{\"id\":1}")` | `method()` is `"POST"`, the `Content-Type` header is `application/json`, the body is 8 bytes, `timeout()` is `Optional[PT30S]` |
