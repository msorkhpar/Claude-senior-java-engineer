An `Instant` holds nanoseconds, but `toEpochMilli()` **truncates** to whole
milliseconds. For full precision the page reads `getEpochSecond()` and
`getNano()`. Note that the nano part is always between 0 and 999,999,999, even
before the epoch: half a second before the epoch is epoch second `-1` plus
500,000,000 nanoseconds.

A log format writes an instant as its time since the epoch in **decimal
seconds with exactly nine decimal places**. Write `EpochStamp`:

- `static String encode(Instant instant)` writes the stamp.
- `static Instant decode(String stamp)` reads a stamp back into the same instant.

| instant | stamp |
|---|---|
| `Instant.ofEpochSecond(1710523800, 123_000_000)` | `"1710523800.123000000"` |
| `Instant.ofEpochSecond(1710523800, 123_456_789)` | `"1710523800.123456789"` |
| `Instant.ofEpochSecond(100, 5)` | `"100.000000005"` |
| `Instant.ofEpochSecond(-1, 500_000_000)` | `"-0.500000000"` |

`java.math.BigDecimal` can hold such a number exactly.
