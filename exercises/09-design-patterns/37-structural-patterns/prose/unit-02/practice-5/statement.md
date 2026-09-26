The page's Q2 calls `java.io` the textbook Decorator: `FilterInputStream` is
the base decorator, and `BufferedInputStream`, `DataInputStream` and
`GZIPInputStream` each wrap another `InputStream` and add one capability.
Write one more: a `CountingInputStream` that passes bytes through unchanged
and counts them.

`CountingInputStream extends FilterInputStream`:

- `getCount()` is the number of bytes the reads have returned so far.
- **A bulk read counts the bytes it returned**, not the length asked for.
- **The end of the stream is not counted**: a read that returns `-1` adds
  nothing.
- **Every read path is counted**: single-byte `read()`, `read(byte[], int, int)`,
  and anything built on them such as `readAllBytes()` or another decorator
  reading through this one.

Where the counter sits in a chain decides what it counts: wrapped by a
`GZIPInputStream`, it counts compressed bytes.

| stream over the bytes of `"hello"` | calls | returns | `getCount()` |
|---|---|---|---|
| `new CountingInputStream(in)` | `read()` five times | `h e l l o` | `5` |
| same | `read(buf, 0, 10)` | `5` | `5` |
| same | `read()` six times | `... o, -1` | `5` |
| same | `readAllBytes()` | 5 bytes | `5` |
