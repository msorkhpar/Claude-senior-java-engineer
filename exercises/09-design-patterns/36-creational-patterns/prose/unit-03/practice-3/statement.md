The warehouse asks a carrier's API for shipping labels. `LabelRequests` holds the settings every
request shares (the API's address, a timeout, the token and the `Accept` header) in one
`HttpRequest.Builder`, and builds the requests that a separate sender sends. The carrier uses the
`X-Request-Id` header to recognise a label requested twice, so each label request must carry
exactly one id, its own, and status requests must carry none.

Finish `label(...)` in `src/main/java/practice/LabelRequests.java`: it returns a POST of the given
JSON with the shared settings and the given request id. `status(...)` already works. Nothing is
sent over the network; the tests read the requests you build. About four lines in total.
