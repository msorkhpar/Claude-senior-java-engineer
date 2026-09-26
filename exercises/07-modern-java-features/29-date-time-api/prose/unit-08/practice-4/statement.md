An old JDBC driver only takes and returns `java.sql.Timestamp` for a
`TIMESTAMP` column, while the application works with `Instant`. Write the two
conversions on `JdbcStamps`:

- `write(Instant instant)`: the `Timestamp` to store.
- `read(Timestamp stamp)`: the `Instant` it holds.

An instant must come back **exactly**, down to the **nanosecond**: an event at
`...:00.123456789Z` must not come back as `...:00.123Z`.

The server's default time zone must make no difference: the two moments at
01:30 on 2024-11-03 in New York, when the clocks repeat an hour, stay two moments.

Columns can be empty, so **null stays null** in both directions.

| call | answer |
|---|---|
| `read(write(2024-03-15T10:30:00.123Z))` | `2024-03-15T10:30:00.123Z` |
| `read(write(2024-03-15T17:30:00.123456789Z))` | `2024-03-15T17:30:00.123456789Z` |
| `write(null)` | `null` |
| `read(null)` | `null` |
