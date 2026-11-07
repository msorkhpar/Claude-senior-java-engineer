# 7.3.1. Limitations of the Legacy Date and Calendar Classes

A booking service written before Java 8 gets three tickets in one week. Monday's standup moved
from 08:00 to 07:45 UTC, and nobody asked for it. A guest booked a room for 30 February and was
charged for 2 March. The nightly report puts a booking made at 23:30 UTC on Sunday on one server
and on Monday on another. Each comes from how `java.util.Date`, `Calendar` or `SimpleDateFormat`
behave by default.

Every output below comes from Java 21. Each run sets the default zone and locale explicitly:
`-Duser.timezone=Europe/Amsterdam` unless stated, and `-Duser.language=en -Duser.country=US`.

## A Date Is a Mutable Moment, So Anyone Holding It Can Move It

A `Date` holds one `long`, milliseconds since 1970-01-01T00:00Z, and `setTime` changes it. The
meeting keeps its start in a `private final Date` field and returns it from a getter. The reminder
service wants a time 15 minutes earlier:

```java
static final class Meeting {
    private final Date start;
    Meeting(Date start) { this.start = start; }
    Date getStart() { return start; }
}

Meeting standup = new Meeting(Date.from(Instant.parse("2026-03-16T08:00:00Z")));
Date reminder = standup.getStart();                       // the reminder service's code
reminder.setTime(reminder.getTime() - 15 * 60 * 1000);   // remind 15 minutes before
System.out.println("meeting starts: " + standup.getStart().toInstant());
```

```text
meeting starts: 2026-03-16T07:45:00Z
```

`final` fixes which object the field points to, not what is inside it. The getter handed out the
meeting's own object, so the reminder service moved the meeting itself. The constructor stores the
caller's object too, so a caller that reuses its `Date` for the next booking moves this meeting.

Where you must keep `Date`, copy it in the constructor and in the getter: `new Date(d.getTime())`.
The `java.time` value types are immutable, and their `plus...` methods return a new object
(unit 7.3.2).

<!-- practice 1 -->

## Months Count From 0, Years From 1900, and Out-of-Range Values Roll Over

`Calendar` numbers months 0 to 11, and the deprecated `Date` constructors also take the year minus
1900. By default neither rejects an out-of-range value: the excess carries into the next field.

```java
Date d = new Date(2026, 3, 15);                   // each result printed as yyyy-MM-dd
Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Amsterdam"));
cal.clear(); cal.set(2026, 3, 15);
cal.clear(); cal.set(2026, 12, 1);
cal.clear(); cal.set(2026, Calendar.FEBRUARY, 30);
cal.clear(); cal.setLenient(false); cal.set(2026, Calendar.FEBRUARY, 30);
cal.getTime();                                    // throws; caught and printed
Date march = new Date(126, Calendar.MARCH, 15);
System.out.println("getYear() " + march.getYear() + ", getMonth() " + march.getMonth());
```

```text
new Date(2026, 3, 15):        3926-04-15
cal.set(2026, 3, 15):         2026-04-15
cal.set(2026, 12, 1):         2027-01-01
cal.set(2026, FEBRUARY, 30):  2026-03-02
strict getTime():             java.lang.IllegalArgumentException: MONTH: 1 -> 2
getYear() 126, getMonth() 2
```

The year 2026 passed to the constructor became 3926, month 3 is April, month 12 is January of the
next year, and 30 February is 2 March. The conventions are those of C's `struct tm` (`tm_year`
since 1900, `tm_mon` from 0). Only the non-lenient calendar refused, and only when it next computed
its fields. The bug appears wherever values cross into or out of the legacy API: a column written
from `getMonth()` and read as a calendar month is a month early on every row. `LocalDate.of` counts months from 1 and throws on 30 February.

<!-- practice 2 -->

## Parsing Is Lenient by Default, and Strict Mode Still Ignores What Follows the Date

`SimpleDateFormat.parse` uses the same lenient calendar:

```java
SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
f.setTimeZone(TimeZone.getTimeZone("UTC"));
for (String s : new String[] {"2026-02-30", "2026-13-01", "2026-03-15abc"}) {
    System.out.printf("lenient %-14s -> %s%n", s, out.format(f.parse(s)));   // out: the same pattern, UTC
}
f.setLenient(false);                              // then "2026-02-30", "2026-03-15abc", "2026-3-5"
ParsePosition pos = new ParsePosition(0);
f.parse("2026-03-15abc", pos);
System.out.println("parse stopped at index " + pos.getIndex() + " of 13");
```

```text
lenient 2026-02-30     -> 2026-03-02
lenient 2026-13-01     -> 2027-01-01
lenient 2026-03-15abc  -> 2026-03-15
strict  2026-02-30     -> java.text.ParseException: Unparseable date: "2026-02-30"
strict  2026-03-15abc  -> 2026-03-15
strict  2026-3-5       -> 2026-03-05
parse stopped at index 10 of 13
```

The lenient formatter is how the guest got 2 March. Strict mode rejects impossible fields, but
`parse(String)` stops reading once it has a date and returns it; the `ParsePosition` shows it
stopped at index 10 of 13. `MM` does not require two digits, so `2026-3-5` passes.
`LocalDate.parse(text)` reads the whole text and validates the date (a pattern formatter needs
`ResolverStyle.STRICT` for the same check, unit 7.3.2.4):

```text
LocalDate.parse: Text '2026-02-30' could not be parsed: Invalid date 'FEBRUARY 30'
```

<!-- practice 3 -->

## One SimpleDateFormat Shared by Threads Returns Wrong Dates

Legacy code often keeps one formatter in a `static final` field. Four threads each parse 10,000
dates (1,000 distinct ones, ten times over) through one shared instance and compare every result
with the text they passed in (run with `-Duser.timezone=UTC`):

```java
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
run("SimpleDateFormat ", t -> sdf.parse(t).toInstant().atOffset(ZoneOffset.UTC).toLocalDate());
DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
run("DateTimeFormatter", t -> LocalDate.parse(t, dtf));
```

```text
SimpleDateFormat  right 12804, wrong date 23468, threw {ArrayIndexOutOfBoundsException=56, NumberFormatException=3672}
  first wrong date: 2010-01-01 came back as 2222-01-01
DateTimeFormatter right 40000, wrong date     0, threw {}
  first wrong date: none
```

A `SimpleDateFormat` keeps a `Calendar` and a `DecimalFormat` in instance fields and uses them as
scratch space on every parse, so concurrent parses overwrite each other's partial results. The
counts vary between runs, but in each one most failures return a date, another thread's or a mix
that nobody passed in, and nothing logs them; the exceptions are the few you would notice. The
class is not synchronized, and its javadoc advises a formatter per thread; pre-Java 8 code used a
`ThreadLocal`, a new instance per call, or a lock. `DateTimeFormatter` is immutable and safe to share (unit 7.3.2.4).

<!-- practice 5 -->

## A Date Has No Zone; toString() Uses the JVM's Default

The same `Date` printed in two JVMs, one started with `-Duser.timezone=Europe/Amsterdam` and one
with `-Duser.timezone=America/New_York`:

```java
Date d = Date.from(Instant.parse("2026-03-15T23:30:00Z"));
System.out.println("getTime():  " + d.getTime());
System.out.println("toString(): " + d);
```

```text
getTime():  1773617400000
toString(): Mon Mar 16 00:30:00 CET 2026
```

```text
getTime():  1773617400000
toString(): Sun Mar 15 19:30:00 EDT 2026
```

Both JVMs hold the same `getTime()`. `toString()`, the deprecated `getDate()` and `getHours()`,
and a `SimpleDateFormat` without `setTimeZone` all use the JVM's default zone. That is the nightly
report: the code that cut bookings into days never named a zone, so each server used its own, and
`java.util` has no date-only type to hold the result. To get a day, name the zone:
`d.toInstant().atZone(zone).toLocalDate()` (zones in unit 7.3.2.5).

## java.sql.Date Is a java.util.Date That Breaks Its Parent's Contract

JDBC's date-only type, `java.sql.Date`, extends `java.util.Date` and still holds milliseconds.
Its `toString()` prints only the day:

```java
long nineUtc = Instant.parse("2026-03-15T09:00:00Z").toEpochMilli();
java.sql.Date fromMillis = new java.sql.Date(nineUtc);
java.sql.Date fromText = java.sql.Date.valueOf("2026-03-15");
System.out.println(fromMillis + " equals " + fromText + ": " + fromMillis.equals(fromText));
System.out.println("getTime(): " + fromMillis.getTime() + " vs " + fromText.getTime());
java.util.Date asUtil = fromMillis;               // it IS a java.util.Date
asUtil.toInstant();                               // throws; caught and printed
asUtil.getHours();                                // throws; caught and printed
System.out.println("Instant.ofEpochMilli(getTime()): " + Instant.ofEpochMilli(asUtil.getTime()));
System.out.println("fromText as a moment, in UTC: "
        + Instant.ofEpochMilli(fromText.getTime()).atZone(ZoneOffset.UTC).toLocalDate());
System.out.println("fromText.toLocalDate():       " + fromText.toLocalDate());
```

```text
2026-03-15 equals 2026-03-15: false
getTime(): 1773565200000 vs 1773529200000
toInstant(): java.lang.UnsupportedOperationException
getHours(): java.lang.IllegalArgumentException
Instant.ofEpochMilli(getTime()): 2026-03-15T09:00:00Z
fromText as a moment, in UTC: 2026-03-14
fromText.toLocalDate():       2026-03-15
```

The constructor keeps the time of day and `equals` compares `getTime()`, so two values that print
the same day aren't equal, and a `Set` of them keeps both. The subclass also throws from methods its parent declares: a method that
takes a `java.util.Date`, calls `toInstant()` and is tested only with `java.util.Date` values fails
on the first value from `ResultSet.getDate` (`java.sql.Timestamp` doesn't throw). For a moment,
`Instant.ofEpochMilli(d.getTime())` works on both types. A `java.sql.Date` names a day, though:
`valueOf` stored midnight in the default zone, 23:00Z the day before, so reading it as a moment in
another zone gives the wrong day, and `toLocalDate()` gives the right one. Conversions are unit
7.3.3.

<!-- practice 4 -->

## Summary

- **A `Date` is mutable**: a getter or constructor that shares one lets any caller move your
  moment, and `final` doesn't stop it.
- **Months count from 0, years from 1900, and `Calendar` rolls overflow** into the next field
  instead of rejecting it.
- **`SimpleDateFormat` parses leniently by default**, and strict mode still ignores text after
  the date.
- **A shared `SimpleDateFormat` returns wrong dates under concurrency**, most of them silently.
- **A `Date` has no zone or day**: `toString()` and `Date`'s deprecated field getters use the JVM
  default.
- **`java.sql.Date` breaks `java.util.Date`'s contract**: `toInstant()` throws, values that print
  the same day can differ, and its day comes from `toLocalDate()`.

## Reference

**Legacy behaviour (Java 21, from runs or the javadoc):**

| Call | Result |
|---|---|
| `new Date(y, m, d)` (deprecated) | year `1900 + y`, month `m` from 0 |
| `date.getYear()`, `getMonth()` | year minus 1900; month from 0 |
| `Calendar.set(y, 12, 1)` (lenient, the default) | January of `y + 1` |
| `Calendar.setLenient(false)` | out-of-range fields throw `IllegalArgumentException` when the time is computed |
| `SimpleDateFormat.parse(String)` | lenient by default; ignores text after the date even when strict |
| `TimeZone.getTimeZone("Europe/Amsterdm")` | `GMT`: an unknown ID is not an error |
| `TimeZone.getTimeZone("CST")` | accepted; the javadoc deprecates three-letter IDs as ambiguous |
| `java.sql.Date.toInstant()`, `getHours()` | `UnsupportedOperationException`, `IllegalArgumentException` |

**Replacements:**

| Legacy | `java.time` | Unit |
|---|---|---|
| `Date` as a moment | `Instant` | 7.3.2.2 |
| `Date` or `java.sql.Date` as a day | `LocalDate` | 7.3.2.1 |
| `Calendar` with a zone | `ZonedDateTime` | 7.3.2.5 |
| `SimpleDateFormat` | `DateTimeFormatter` | 7.3.2.4 |
| `TimeZone` | `ZoneId` | 7.3.2.5 |

## Challenges

1. A library you can't change returns `Date` from its getters and keeps a reference. Where in your
   code do you convert, and what do you lose if a caller still needs the library's object?
2. Rerun the shared-formatter test with one thread and with 32. What do the counts tell you about
   catching this bug in a test suite?
3. A table stores `Date.getMonth()` values in a column named `month`. How do you migrate its
   readers without a day on which both conventions are live?

## Design Note: One Type for Every Question

`java.util.Date` answers one question, "which millisecond", but the 1.0 API also made it answer
"which day" and "which hour" through methods that used the default zone. JDK 1.1 deprecated those
methods and moved calendar work to `Calendar` and text to `DateFormat`, but kept every type mutable
and lenient by default. `java.time` (JSR 310, Java 8) gives each question its own immutable type,
so a moment and a day have different types. The cost is at the boundary: legacy APIs still take
and return `Date`, and converting to a day or a local time there needs an explicit zone.

## Code Examples

- Test: [LegacyDateLimitationsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datetimeapi/LegacyDateLimitationsTest.java)
- Source: [LegacyDateLimitations.java](src/main/java/com/github/msorkhpar/claudejavatutor/datetimeapi/LegacyDateLimitations.java)
