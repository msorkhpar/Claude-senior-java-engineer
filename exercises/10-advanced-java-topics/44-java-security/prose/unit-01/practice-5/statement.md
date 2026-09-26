Reading a whole user-supplied file lets one huge upload exhaust memory. The
page's fix **bounds the read itself**: read at most `limit + 1` bytes, and if
more than `limit` arrived, refuse. A size checked beforehand (`Files.size`,
`available()`) is not enough, because what is actually read can differ.
Try-with-resources makes sure the stream is **closed on every path**.

Write `BoundedReader.read(in, limit)` that reads `in` as UTF-8 text:

- up to `limit` bytes the text is returned;
- more than `limit` bytes is refused with `SecurityException` (never a
  truncated text);
- no more than `limit + 1` bytes are ever taken from `in`;
- `in` is closed whether the read succeeds or not.

| upload | limit | result |
|---|---|---|
| `"hello, world"` (12 bytes) | 100 | `"hello, world"` |
| 10 bytes | 10 | the text |
| 11 bytes | 10 | `SecurityException` |
| 1000 bytes from a stream whose `available()` says 0 | 10 | `SecurityException`, at most 11 bytes read |
