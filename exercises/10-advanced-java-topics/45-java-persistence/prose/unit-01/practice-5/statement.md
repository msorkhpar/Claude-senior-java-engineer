Calling `executeUpdate()` once per row costs one round trip per row. The
page's fix queues rows with `addBatch()` and sends them with
`executeBatch()`, in chunks (the page uses 1000) so a huge input does not pile
up in memory.

Write `BatchInsert.insertAll(connection, names, chunkSize)`. It inserts each
name into `people(name)` with one `PreparedStatement` and returns the number
of rows inserted (the sum of the counts `executeBatch()` returns).

- **One `executeBatch()` per chunk** of `chunkSize` rows, never one call per row.
- **The last, partial chunk is sent as well.**
- **A `chunkSize` below 1 is refused** with `IllegalArgumentException`.

| names | chunkSize | rows inserted | `executeBatch()` calls |
|---|---|---|---|
| Ada, Linus, Grace, Barbara | 2 | 4 | 2 |
| Ada, Linus, Grace, Barbara, Ken | 2 | 5 | 3 |
| Ada | 0 | `IllegalArgumentException` | 0 |
