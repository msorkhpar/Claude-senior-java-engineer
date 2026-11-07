Sensors upload their readings to the ingest API, some compressed. `BodyDecoder` looks at the
request's `Content-Encoding` header and wraps the body in the matching decoder. The API accepts
`identity`, `gzip` (and its old alias `x-gzip`) and `deflate`. The firmware of one vendor sends
`GZIP`, an older gateway sends no `Content-Encoding` header at all for uncompressed bodies, and
the service is about to be deployed to a data centre in Istanbul whose servers run with a
Turkish default locale.

Write `codecFor(String contentEncoding)` in `src/main/java/practice/BodyDecoder.java` with a
`switch` statement, so every one of those clients is understood, and an encoding the API doesn't
accept is refused with `IllegalArgumentException`. About ten lines in total.
