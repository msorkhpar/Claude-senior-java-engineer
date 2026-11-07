# 7.3.2.2. Instant and Timestamps

A document service answers the first edit of every new document with `409 Conflict`, and the
second edit of the same document always goes through. Creating a document stamps it with
`updatedAt = Instant.now()` and returns the stamp to the client. An edit sends the stamp back, and
the save rejects it unless it equals the `updatedAt` column. Nobody else touched the document.

Every output below is from Java 21 (tzdata 2026b), run with `-Duser.timezone=UTC`. The code names
every zone it uses, and apart from the one timing run, the outputs are the same under
`-Duser.timezone=Asia/Kolkata`.

## An Instant Is Seconds and Nanoseconds Counted From 1970-01-01T00:00Z

An `Instant` holds two numbers: a `long` of seconds since the epoch, and an `int` of nanoseconds
within that second, from 0 to 999,999,999. It has no zone and no calendar.

```java
Instant stamp = Instant.parse("2026-03-14T09:30:00.123456789Z");
Instant early = Instant.parse("1969-12-31T23:59:59.5Z");
for (Instant t : List.of(stamp, early)) {
    System.out.println(t + "  seconds=" + t.getEpochSecond() + " nanos=" + t.getNano()
            + " millis=" + t.toEpochMilli());
}
```

```text
2026-03-14T09:30:00.123456789Z  seconds=1773480600 nanos=123456789 millis=1773480600123
1969-12-31T23:59:59.500Z  seconds=-1 nanos=500000000 millis=-500
```

Half a second before the epoch is second -1 plus half a second, because the nanosecond part never
goes negative. Writing it as `getEpochSecond() + "." + getNano()` gives `-1.500000000`, which reads
as -1.5 s. `toEpochMilli()` drops the digits below the millisecond, rounding towards the past.

An epoch number on the wire carries no unit. A carrier's webhook sends `"ts": 1773480600`, in
seconds, to a handler written for milliseconds:

```java
long ts = 1773480600L;                       // the partner's "ts" field, in seconds
System.out.println("ofEpochSecond(ts):        " + Instant.ofEpochSecond(ts));
System.out.println("ofEpochMilli(ts):         " + Instant.ofEpochMilli(ts));
System.out.println("ofEpochSecond(ts * 1000): " + Instant.ofEpochSecond(ts * 1000));
```

```text
ofEpochSecond(ts):        2026-03-14T09:30:00Z
ofEpochMilli(ts):         1970-01-21T12:38:00.600Z
ofEpochSecond(ts * 1000): +58169-05-12T20:00:00Z
```

Nothing throws. Read as milliseconds, today's events land in January 1970, and a 90-day cleanup
deletes them that night. Read the other way (third line), they never expire. Put the unit in the
field's name or the contract (`tsMillis`), and call the factory that matches it.

## Instant.parse Accepts Only Text That Names Its Offset

`Instant.parse` reads the ISO form with a `Z` or a numeric offset, and converts an offset to UTC.
Without one it refuses:

```java
for (String text : List.of("2026-03-14T09:30:00Z", "2026-03-14T10:30:00+01:00", "2026-03-14T10:30:00")) {
    try {
        System.out.println(text + "  ->  " + Instant.parse(text));
    } catch (DateTimeParseException e) {
        System.out.println(text + "  ->  " + e.getMessage());
    }
}
```

```text
2026-03-14T09:30:00Z  ->  2026-03-14T09:30:00Z
2026-03-14T10:30:00+01:00  ->  2026-03-14T09:30:00Z
2026-03-14T10:30:00  ->  Text '2026-03-14T10:30:00' could not be parsed at index 19
```

Text without an offset is a wall-clock reading (unit 7.3.2.1), and it names a different moment in
every zone, so `Instant` won't guess. A depot in Amsterdam sends its scanner's local time, and the
quick fix appends `Z`:

```java
String scan = "2026-03-14T10:30:00";         // local time at the Amsterdam depot
System.out.println("scan + \"Z\":     " + Instant.parse(scan + "Z"));
System.out.println("scan in its zone: "
        + LocalDateTime.parse(scan).atZone(ZoneId.of("Europe/Amsterdam")).toInstant());
```

```text
scan + "Z":     2026-03-14T10:30:00Z
scan in its zone: 2026-03-14T09:30:00Z
```

Appending `Z` declares the text UTC, and every scan lands an hour late, two in summer. A parcel
scanned before the 18:00 cut-off shows as scanned after it. The receiver has to be told the
sender's zone. Local times that fall in a DST gap or overlap are unit 7.3.2.5.

<!-- practice 1 -->

## A Timestamp Survives a Store Only at the Store's Precision

On Java 21 on Linux, `Instant.now()` fills the nanoseconds:

```java
int finerThanMicros = 0, finerThanMillis = 0;
long smallestStep = Long.MAX_VALUE;
Instant prev = Instant.now();
for (int i = 0; i < 1_000_000; i++) {
    Instant now = Instant.now();
    if (now.getNano() % 1_000 != 0) finerThanMicros++;
    if (now.getNano() % 1_000_000 != 0) finerThanMillis++;
    long step = Duration.between(prev, now).toNanos();
    if (step > 0 && step < smallestStep) smallestStep = step;
    prev = now;
}
System.out.println("Instant.now(), 1,000,000 calls:");
System.out.println("  with digits below the millisecond: " + finerThanMillis);
System.out.println("  with digits below the microsecond: " + finerThanMicros);
System.out.println("  smallest step between two calls:   " + smallestStep + " ns");
```

```text
Instant.now(), 1,000,000 calls:
  with digits below the millisecond: 999999
  with digits below the microsecond: 998945
  smallest step between two calls:   20 ns
```

The counts change from run to run and machine to machine. `Instant.now()` reads
`Clock.systemUTC()`, whose javadoc promises only "the best available system clock", possibly
`System.currentTimeMillis()`. Many stores are coarser, such as a column of epoch milliseconds or a
`java.util.Date` (unit 7.3.3). The document service's create and first save, on a fixed clock
(unit 7.3.2):

```java
Clock clock = Clock.fixed(Instant.parse("2026-03-14T09:30:00.123456789Z"), ZoneOffset.UTC);

Instant created = Instant.now(clock);        // returned to the client with the new document
long column = created.toEpochMilli();        // what a millisecond column keeps
Instant stored = Instant.ofEpochMilli(column);
System.out.println("client sends back: " + created);
System.out.println("column holds:      " + stored);
System.out.println("equal: " + created.equals(stored) + ", apart by "
        + Duration.between(stored, created).toNanos() + " ns");
```

```text
client sends back: 2026-03-14T09:30:00.123456789Z
column holds:      2026-03-14T09:30:00.123Z
equal: false, apart by 456789 ns
```

The client holds the value from memory, the column a truncated copy, and `equals` compares
nanoseconds. After the 409 the client reloads, so its second edit carries the column's stamp.
Truncate where the stamp is made, so the value in memory is the one the store keeps:

```java
Instant stamped = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS);
System.out.println("truncated when stamped, equal: "
        + stamped.equals(Instant.ofEpochMilli(stamped.toEpochMilli())));
```

```text
truncated when stamped, equal: true
```

`Clock.tickMillis(zone)` gives a clock that does the same for every reading. Truncating further, to
`SECONDS`, is not safer: two edits in the same second get the same stamp, and a check by stamp lets
the stale one overwrite the other.

<!-- practice 2 -->

## An Instant Has No Calendar, So Months Need a Zone

A subscription renews one month after payment. The payment is an `Instant`:

```java
Instant paid = Instant.parse("2026-01-30T23:30:00Z");
System.out.println("plus(1, DAYS):    " + paid.plus(1, ChronoUnit.DAYS));
try {
    paid.plus(1, ChronoUnit.MONTHS);
} catch (UnsupportedTemporalTypeException e) {
    System.out.println("plus(1, MONTHS):  " + e.getMessage());
}
try {
    paid.get(ChronoField.MONTH_OF_YEAR);
} catch (UnsupportedTemporalTypeException e) {
    System.out.println("get(MONTH_OF_YEAR): " + e.getMessage());
}

System.out.println("plus(30, DAYS):   " + paid.plus(30, ChronoUnit.DAYS));
for (String zone : List.of("UTC", "Asia/Tokyo")) {
    ZonedDateTime there = paid.atZone(ZoneId.of(zone));
    System.out.println(zone + ": paid " + there.toLocalDateTime() + ", renews "
            + there.plusMonths(1).toInstant());
}
```

```text
plus(1, DAYS):    2026-01-31T23:30:00Z
plus(1, MONTHS):  Unsupported unit: Months
get(MONTH_OF_YEAR): Unsupported field: MonthOfYear
plus(30, DAYS):   2026-03-01T23:30:00Z
UTC: paid 2026-01-30T23:30, renews 2026-02-28T23:30:00Z
Asia/Tokyo: paid 2026-01-31T08:30, renews 2026-02-27T23:30:00Z
```

The length of a month depends on which month it is, and the date depends on the zone. Added in UTC,
this renewal lands on 1 March, 08:30 in Tokyo; added in Tokyo, on 28 February. `DAYS` works only
because the javadoc defines it as 86,400 seconds, which is not a calendar day across a DST change
(unit 7.3.2.3).

The usual workaround, `plus(30, DAYS)`, renews this payment on 1 March. Add the month in the
customer's zone and convert back.

<!-- practice 3 -->

## Summary

- **An `Instant` is epoch seconds plus nanoseconds.** An epoch number read in the wrong unit
  gives a wrong instant, and nothing throws.
- **`Instant.parse` needs a `Z` or an offset.** Text without one names no moment; appending `Z`
  misreads the sender's local time as UTC.
- **A stored timestamp keeps only the store's precision.** Truncate when you stamp, to the store's
  unit and no coarser, or equality fails after the round trip.
- **An `Instant` has no months.** Add them in the customer's zone, then convert back.

## Reference

| Call (Java 21) | Behaviour |
|---|---|
| `ofEpochSecond(s)`, `ofEpochSecond(s, nanoAdjustment)`, `ofEpochMilli(ms)` | build from epoch numbers; the adjustment may be negative or over a second |
| `getEpochSecond()`, `getNano()` | seconds (negative before 1970) and 0 to 999,999,999 nanoseconds |
| `toEpochMilli()` | drops sub-millisecond digits towards the past; `ArithmeticException` on `long` overflow |
| `Instant.parse(text)` | `ISO_INSTANT`: seconds required, 0 to 9 fraction digits, `Z` or an offset (converted to UTC); a trailing `[Europe/Amsterdam]` is refused |
| `toString()` | always `Z`, always seconds, 0, 3, 6 or 9 fraction digits |
| `plus`/`minus` with a `ChronoUnit` | `NANOS` to `DAYS` (`DAYS` = 86,400 s); `WEEKS` and longer throw |
| `truncatedTo(unit)` | up to `DAYS` (UTC midnight); a larger unit throws |

## Challenges

1. A service's database keeps microseconds and its event stream milliseconds. Which precision do
   you stamp with, and where do you enforce it?
2. A feed sends epoch numbers with no unit, and you can't change it. Can you tell seconds from
   milliseconds by size, and for which dates would that guess fail?
3. NTP can move the clock behind `Instant.now()`. What do you use to time a request, and why?

## Design Note: A Day Is Always 86,400 Seconds

The `Instant` javadoc defines the Java time-scale: every day has exactly 86,400 seconds, and on a
day with a leap second that second is spread over the day's last 1,000 seconds (UTC-SLS). So
`Instant` can't name 23:59:60, and an interval across a leap second is a second shorter than atomic
time says. In exchange, an `Instant` converts to any date by integer arithmetic, with no table
of leap seconds. The javadoc doesn't require the system clock to perform the spreading, so around
a leap second `Instant.now()` does whatever the operating system does.

## Code Examples

- Test: [InstantTimestampExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datetimeapi/InstantTimestampExamplesTest.java)
- Source: [InstantTimestampExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/datetimeapi/InstantTimestampExamples.java)
