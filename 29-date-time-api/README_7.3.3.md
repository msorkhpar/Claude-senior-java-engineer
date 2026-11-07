# 7.3.3. Interoperability with Legacy Date and Calendar Classes

A payments service uses `java.time` everywhere in its own code. It still writes every payment to
an audit library that stores `java.util.Date`, and to an old JDBC layer of `java.sql.Timestamp`
columns. The batch host moved from a data centre in Amsterdam to a container in New York. Since
then the nightly reconciliation flags every payment as changed, and payments made late in the UTC
evening land on another day in the settlement report. Nobody changed the code.

Outputs are from Java 21.0.12.1 with tzdata 2026b, run with
`-Duser.language=en -Duser.country=US`, once under `-Duser.timezone=Europe/Amsterdam` and once under
`America/New_York`. Where the two differ, the page shows both.

## Date and Instant Name the Same Moment, but a Date Keeps Only Milliseconds

`Date.from(instant)` and `date.toInstant()` need no zone, because both types count from
1970-01-01T00:00Z. They don't count in the same unit:

```java
Instant event = Instant.parse("2026-05-04T09:15:30.123456789Z");
Instant viaDate = Date.from(event).toInstant();
Instant viaTimestamp = Timestamp.from(event).toInstant();
System.out.println(viaDate + "       equals: " + viaDate.equals(event));
System.out.println(viaTimestamp + " equals: " + viaTimestamp.equals(event));
```

```text
2026-05-04T09:15:30.123Z       equals: false
2026-05-04T09:15:30.123456789Z equals: true
```

A `Date` holds milliseconds, so `Date.from` drops the rest; `Timestamp` keeps a separate
nanosecond field. `Instant.now()` on Linux fills digits below the millisecond (unit 7.3.2.2), so almost every
payment comes back from the audit library as a different `Instant`, and `equals` reports a
change.

Compare at the precision of the narrower side: `date.getTime() == instant.toEpochMilli()`, or
`instant.truncatedTo(ChronoUnit.MILLIS)` before `equals`. Both truncate, like `Date.from`;
rounding would turn .123987 into .124 and report a change again.

<!-- practice 1 -->

## Timestamp and java.sql.Date Turn Local Values into Moments in the JVM's Default Zone

`Timestamp` and `java.sql.Date` are moments too, but they also convert to and from local values,
with no zone argument. The same code under `Europe/Amsterdam`, then `America/New_York`:

```java
Instant paid = Instant.parse("2026-05-04T22:30:00Z");
LocalDateTime wall = LocalDateTime.parse("2026-05-04T22:30");
LocalDate day = LocalDate.parse("2026-05-04");
System.out.println("Timestamp.from(paid).toLocalDateTime(): " + Timestamp.from(paid).toLocalDateTime());
System.out.println("Timestamp.valueOf(wall).toInstant():    " + Timestamp.valueOf(wall).toInstant());
System.out.println("java.sql.Date.valueOf(day).getTime():   "
        + Instant.ofEpochMilli(java.sql.Date.valueOf(day).getTime()));
```

```text
Timestamp.from(paid).toLocalDateTime(): 2026-05-05T00:30
Timestamp.valueOf(wall).toInstant():    2026-05-04T20:30:00Z
java.sql.Date.valueOf(day).getTime():   2026-05-03T22:00:00Z
```

```text
Timestamp.from(paid).toLocalDateTime(): 2026-05-04T18:30
Timestamp.valueOf(wall).toInstant():    2026-05-05T02:30:00Z
java.sql.Date.valueOf(day).getTime():   2026-05-04T04:00:00Z
```

The javadoc of `valueOf(LocalDateTime)` and `toLocalDateTime()` says the local value is read "in
the local time zone", the JVM's default. The settlement report used
`Timestamp.from(paid).toLocalDateTime().toLocalDate()`, so this payment belonged to 5 May in
Amsterdam and to 4 May in New York. A `java.sql.Date` is midnight in the default zone, so its
milliseconds sent to a JVM in another zone (through a cache or a message) can read back there as
the day before.

Use the bridges in pairs: `from`/`toInstant` for a moment, `valueOf`/`toLocalDateTime` (and
`toLocalDate`) for a wall-clock value. Mixing them shifts the value by the default zone's
offset. For a wall-clock column in a known zone, do the zone step in `java.time`
(`LocalDateTime.ofInstant(paid, zone)`) and hand `valueOf` only the result.

A pair that matches still changes a wall-clock time the default zone skips. Under
`Europe/Amsterdam`, around the 2026 clock changes:

```java
ZoneId amsterdam = ZoneId.of("Europe/Amsterdam");
LocalDateTime gap = LocalDateTime.parse("2026-03-29T02:30");      // clocks jump 02:00 -> 03:00
LocalDateTime overlap = LocalDateTime.parse("2026-10-25T02:30");  // 02:00-03:00 happens twice
System.out.println("gap, round trip:     " + Timestamp.valueOf(gap).toLocalDateTime());
System.out.println("overlap, Timestamp:  " + Timestamp.valueOf(overlap).toInstant());
System.out.println("overlap, atZone:     " + overlap.atZone(amsterdam).toInstant());
```

```text
gap, round trip:     2026-03-29T03:30
overlap, Timestamp:  2026-10-25T01:30:00Z
overlap, atZone:     2026-10-25T00:30:00Z
```

A booking stored at 02:30 comes back at 03:30. In the overlap, `Timestamp` takes the later offset
and `java.time` the earlier one (unit 7.3.2.5), so two parts of one service store the same local
time as moments an hour apart.

> **JDBC 4.2 drivers.** The JDBC 4.2 specification lets `setObject` take `LocalDate`,
> `LocalDateTime` and `OffsetDateTime`, and `getObject(column, LocalDateTime.class)` return them,
> with no `Timestamp` in your code. Whether a driver goes through the default zone inside is up to
> the driver; this unit runs none, so test yours.

<!-- practice 2 -->

## Build a TimeZone from a ZoneId, So a Wrong ID Fails

`TimeZone.getTimeZone(ZoneId)` and `timeZone.toZoneId()` convert between the two zone types.
How you build a `TimeZone` from a configured ID decides what a typo does:

```java
System.out.println(TimeZone.getTimeZone("Europe/Amsterdm").getID());
TimeZone.getTimeZone(ZoneId.of("Europe/Amsterdm"));             // throws; caught and printed
System.out.println(TimeZone.getTimeZone("PST").toZoneId() + ", " + TimeZone.getTimeZone("EST").toZoneId());
```

```text
GMT
java.time.zone.ZoneRulesException: Unknown time-zone ID: Europe/Amsterdm
America/Los_Angeles, -05:00
```

`TimeZone.getTimeZone(String)` answers an unknown ID with GMT, so a service
configured with a typo runs on UTC all year and nothing fails. `ZoneId.of` rejects it at startup.
The old three-letter IDs convert, not always as the name suggests: `EST` becomes a fixed `-05:00`
with no summer time, and `ZoneId.of("PST")` throws.

## A Calendar Converts With Its Own Zone, Not the JVM's

A `Calendar` carries a `TimeZone`. `GregorianCalendar.from(ZonedDateTime)` sets it from the zone,
and `toZonedDateTime()` reads it back. `calendar.toInstant()` keeps only the moment, so the zone
you attach next decides the hour. Two runs again, Amsterdam then New York:

```java
ZonedDateTime pickup = ZonedDateTime.of(2026, 5, 4, 9, 0, 0, 0, ZoneId.of("Asia/Kolkata"));
GregorianCalendar cal = GregorianCalendar.from(pickup);
System.out.println(cal.getTimeZone().getID() + ", hour " + cal.get(Calendar.HOUR_OF_DAY));
System.out.println("toZonedDateTime():            " + cal.toZonedDateTime());
System.out.println("toInstant(), default zone:    " + cal.toInstant().atZone(ZoneId.systemDefault()));
```

```text
Asia/Kolkata, hour 9
toZonedDateTime():            2026-05-04T09:00+05:30[Asia/Kolkata]
toInstant(), default zone:    2026-05-04T05:30+02:00[Europe/Amsterdam]
```

```text
Asia/Kolkata, hour 9
toZonedDateTime():            2026-05-04T09:00+05:30[Asia/Kolkata]
toInstant(), default zone:    2026-05-03T23:30-04:00[America/New_York]
```

A 09:00 pickup in Kolkata shows as 05:30, or as 23:30 the day before. Only `GregorianCalendar` has
`toZonedDateTime()`; for any other `Calendar`, use
`cal.toInstant().atZone(cal.getTimeZone().toZoneId())`. The old way in,
`new GregorianCalendar()` and then `setTime(date)`, has the same flaw: it takes the default zone.

`from()` also brings ISO week rules: weeks start on Monday, and week 1 is the first with four days
in the new year. A report keyed on `WEEK_OF_YEAR` changes when its calendars start coming from
`from()`:

```java
ZonedDateTime newYear = ZonedDateTime.of(2027, 1, 1, 12, 0, 0, 0, ZoneId.of("America/New_York"));
GregorianCalendar us = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"), Locale.US);
us.setTimeInMillis(newYear.toInstant().toEpochMilli());
System.out.println("WEEK_OF_YEAR, from(): " + GregorianCalendar.from(newYear).get(Calendar.WEEK_OF_YEAR)
        + ", Locale.US: " + us.get(Calendar.WEEK_OF_YEAR));
```

```text
WEEK_OF_YEAR, from(): 53, Locale.US: 1
```

1 January 2027 is a Friday, so under ISO rules it belongs to the last week of 2026.

<!-- practice 3 -->

## Summary

- **A `Date` keeps milliseconds.** An `Instant` that goes through one loses the rest, so compare
  at millisecond precision, truncating; `Timestamp` keeps nanoseconds.
- **`valueOf`, `toLocalDateTime` and `toLocalDate` use the JVM's default zone.** Use
  `from`/`toInstant` for moments, do any other zone step in `java.time`, and expect a DST gap to
  move a wall-clock time.
- **`TimeZone.getTimeZone(String)` turns an unknown ID into GMT.** Build the `TimeZone` from
  `ZoneId.of`.
- **A `Calendar` converts with its own zone**: `GregorianCalendar.from`/`toZonedDateTime`, or
  `toInstant()` plus `getTimeZone().toZoneId()`. `from()` brings ISO week rules.

## Reference

**Bridges (Java 21, from runs or the javadoc):**

| From ↔ to | Calls | Zone used | Note |
|---|---|---|---|
| `Instant` ↔ `Date` | `Date.from(i)`, `d.toInstant()` | none | milliseconds; `Date.from` throws `IllegalArgumentException` outside `Date`'s range |
| `Instant` ↔ `Timestamp` | `Timestamp.from(i)`, `ts.toInstant()` | none | nanoseconds |
| `LocalDateTime` ↔ `Timestamp` | `Timestamp.valueOf(ldt)`, `ts.toLocalDateTime()` | default | a gap time moves forward; an overlap takes the later offset |
| `LocalDate` ↔ `java.sql.Date` | `java.sql.Date.valueOf(ld)`, `d.toLocalDate()` | default | midnight in the default zone |
| `ZonedDateTime` ↔ `GregorianCalendar` | `GregorianCalendar.from(z)`, `gc.toZonedDateTime()` | the calendar's | `from` gives a pure Gregorian calendar with ISO weeks |
| `ZoneId` ↔ `TimeZone` | `TimeZone.getTimeZone(zoneId)`, `tz.toZoneId()` | — | `getTimeZone(String)` gives GMT for an unknown ID |

On Java 21.0.12.1,
`Timestamp.from(Instant.MAX)` returned a wrong date, not the `IllegalArgumentException` its
javadoc promises.

## Challenges

1. The audit library returns `Date`s that someone else's code already rounded. How do you
   reconcile against them, and what false matches can you get?
2. A `TIMESTAMP` column was written for years by servers in two zones. How do you find which rows
   were written where, and what do you migrate the column to?
3. Which calls that use the JVM's default zone can a static check find, and which only a test
   run under two `-Duser.timezone` values?

## Design Note: Why the Bridges Live on the Legacy Classes

Java 8 put every conversion on the old classes (`Date.from`, `Timestamp.valueOf`,
`GregorianCalendar.from`, `TimeZone.toZoneId`), and no public `java.time` type mentions `Date`,
`Calendar`, `TimeZone` or `java.sql`. The new API stays free of the old one, and the legacy types
can go without touching `java.time`. The cost is that each bridge keeps its old class's rules,
so convert once, where the legacy API is called, with the zone as an argument.

## Code Examples

- Test: [LegacyInteroperabilityTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datetimeapi/LegacyInteroperabilityTest.java)
- Source: [LegacyInteroperability.java](src/main/java/com/github/msorkhpar/claudejavatutor/datetimeapi/LegacyInteroperability.java)
