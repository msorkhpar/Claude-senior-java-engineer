# 7.3.2.1. LocalDate, LocalTime, and LocalDateTime

A customer's first loan instalment was due on 31 January 2026, and her contract says "the last
day of each month". Her schedule asks for 28 February, 28 March, 28 April and 28 May. The code
that built it adds one month to the previous due date. Nothing threw, and every date is valid.

`LocalDate` holds a date, `LocalTime` a time of day, `LocalDateTime` both. None of them holds a
zone or an offset, and all three are immutable values (unit 7.3.2). Every output below comes from
Java 21 (tzdata 2026b), run with `-Duser.timezone=UTC`; a second run with
`-Duser.timezone=Asia/Tokyo` printed the same lines, because no example reads the default zone.

## A LocalDateTime Is a Wall-Clock Reading, and the Same One in Two Zones Is Two Moments

A reminders table stores `2026-03-27T09:00` in a `LocalDateTime` column. A job turns it into a
moment with the zone of the server it runs on:

```java
LocalDateTime call = LocalDateTime.of(2026, 3, 27, 9, 0);   // the column holds this
for (String zone : List.of("Europe/Amsterdam", "America/New_York")) {
    System.out.println(call + " in " + zone + " = " + call.atZone(ZoneId.of(zone)).toInstant());
}
try {
    Instant.from(call);
} catch (DateTimeException e) {
    System.out.println(e.getMessage());
}
```

```text
2026-03-27T09:00 in Europe/Amsterdam = 2026-03-27T08:00:00Z
2026-03-27T09:00 in America/New_York = 2026-03-27T13:00:00Z
Unable to obtain Instant from TemporalAccessor: 2026-03-27T09:00 of type java.time.LocalDateTime
```

The two cities' clocks show 09:00 at different moments, here five hours apart, not the usual
six, because New York had moved its clocks on 8 March and Amsterdam would only on 29 March
(unit 7.3.2.5). `Instant.from` refuses because
the value doesn't say which clock it was read from.

Moving the job from the Amsterdam server to a New York one makes every reminder fire five or six
hours late, and no row changes. Store a moment as an `Instant` (unit 7.3.2.2). Keep a
`LocalDateTime` for what is meant as a local reading, such as opening hours or an appointment,
and keep the zone beside it.

## plusMonths Clamps to a Shorter Month's Last Day, So Step From the First Date

The loan schedule, built both ways:

```java
LocalDate first = LocalDate.of(2026, 1, 31);
LocalDate chained = first;
for (int n = 1; n <= 4; n++) {
    chained = chained.plusMonths(1);                       // from the previous due date
    System.out.println(n + ": chained " + chained + ", from the first " + first.plusMonths(n));
}
System.out.println("plus one month, minus one month: " + first.plusMonths(1).minusMonths(1));
```

```text
1: chained 2026-02-28, from the first 2026-02-28
2: chained 2026-03-28, from the first 2026-03-31
3: chained 2026-04-28, from the first 2026-04-30
4: chained 2026-05-28, from the first 2026-05-31
plus one month, minus one month: 2026-01-28
```

`plusMonths` changes the month and keeps the day if the new month has it. February 2026 has no
31st, so the result is clamped to the 28th. Nothing in a `LocalDate` records the 31st, so every
later step starts from the 28th, and adding a month then subtracting it gives 28 January.

`first.plusMonths(n)` clamps only the month that needs it. Compute each date of a series from
its first date and its index, never from the previous result. Chained, this customer pays up to
three days early every month.

## The Last Day of a Month Comes From an Adjuster, Not a Day Number

A second customer's first instalment is due on 28 February 2026, also "the last day of each
month". Stepping from the first date gives the 28th every month. Compare an adjuster and a fixed
day number:

```java
LocalDate first = LocalDate.of(2026, 2, 28);                // the last day of February
for (int n = 1; n <= 3; n++) {
    System.out.println(n + ": plusMonths " + first.plusMonths(n)
            + ", lastDayOfMonth " + first.plusMonths(n).with(TemporalAdjusters.lastDayOfMonth()));
}
try {
    first.plusMonths(2).withDayOfMonth(31);
} catch (DateTimeException e) {
    System.out.println("withDayOfMonth(31): " + e.getMessage());
}
```

```text
1: plusMonths 2026-03-28, lastDayOfMonth 2026-03-31
2: plusMonths 2026-04-28, lastDayOfMonth 2026-04-30
3: plusMonths 2026-05-28, lastDayOfMonth 2026-05-31
withDayOfMonth(31): Invalid date 'APRIL 31'
```

`withDayOfMonth` sets a field and checks the result: April has no 31st, so it throws
`DateTimeException`, and a schedule job that reaches April stops.
`TemporalAdjusters.lastDayOfMonth()`, applied with `with(...)`, uses the month's length instead
of a day number.

A contract is end-of-month when its first date is its month's last day:
`date.getDayOfMonth() == date.lengthOfMonth()`. A test for day 31 misses the end of every
30-day month and of February.

<!-- practice 1 -->

## LocalDate.parse and LocalDate.of Refuse Dates That Don't Exist

A partner sends due dates as text. Five lines of its file:

```java
for (String text : List.of("2026-02-28", "2026-02-29", "2026-04-31", "2026-2-5", "2026-02-28 ")) {
    try {
        System.out.println("\"" + text + "\" -> " + LocalDate.parse(text));
    } catch (DateTimeParseException e) {
        System.out.println("\"" + text + "\" -> " + e.getMessage());
    }
}
```

```text
"2026-02-28" -> 2026-02-28
"2026-02-29" -> Text '2026-02-29' could not be parsed: Invalid date 'February 29' as '2026' is not a leap year
"2026-04-31" -> Text '2026-04-31' could not be parsed: Invalid date 'APRIL 31'
"2026-2-5" -> Text '2026-2-5' could not be parsed at index 5
"2026-02-28 " -> Text '2026-02-28 ' could not be parsed, unparsed text found at index 10
```

`LocalDate.parse(text)` uses `DateTimeFormatter.ISO_LOCAL_DATE` with its strict resolver: two
digits for month and day, nothing after, and no date the calendar doesn't have.
`LocalDate.of(2026, 2, 29)` throws `DateTimeException` for the same reason. Where
`SimpleDateFormat` turned 30 February into 2 March (unit 7.3.1), the import now stops at the line
with `DateTimeParseException`. The other two refusals are about format: trim the text, or read
`2026-2-5` with a pattern of your own (unit 7.3.2.4). A pattern from `ofPattern` resolves
smartly, not strictly: `uuuu-M-d` reads `2026-4-31` as 30 April. Add
`withResolverStyle(ResolverStyle.STRICT)`, and keep `uuuu`, since strict `yyyy` refuses even
`2026-2-5`.

## datesUntil Leaves Out Its End Date

The March report collects the payments of every day from 1 to 31 March:

```java
LocalDate from = LocalDate.of(2026, 3, 1);
LocalDate to = LocalDate.of(2026, 3, 31);
List<LocalDate> days = from.datesUntil(to).toList();
System.out.println("datesUntil(to): " + days.size() + " days, last " + days.get(days.size() - 1));
List<LocalDate> all = from.datesUntil(to.plusDays(1)).toList();
System.out.println("datesUntil(to.plusDays(1)): " + all.size() + " days, last " + all.get(all.size() - 1));
```

```text
datesUntil(to): 30 days, last 2026-03-30
datesUntil(to.plusDays(1)): 31 days, last 2026-03-31
```

`datesUntil(end)` excludes `end`, like `subList` or `IntStream.range`, so the report missed the
31st's payments. For a period given by its first and last day, pass `last.plusDays(1)`. A hotel
stay already ends on an exclusive date, the check-out day, whose night isn't charged; adding a
day there bills one night too many.

<!-- practice 2 -->

## Summary

- **A `LocalDateTime` is a wall-clock reading, not a moment.** Turning it into one needs a zone,
  and two zones give two instants.
- **`plusMonths` clamps to the last day of a shorter month.** Chained steps drift and don't
  reverse; compute each date from the first one.
- **"The last day of the month" is `TemporalAdjusters.lastDayOfMonth()`.** `withDayOfMonth(31)`
  throws in a 30-day month.
- **`LocalDate.parse(text)` and `LocalDate.of` refuse impossible dates.** A pattern of your own
  needs the strict resolver to do the same.
- **`datesUntil` excludes its end.** An inclusive period needs `last.plusDays(1)`.

## Reference

All from the run in this unit (Java 21):

| Expression | Result |
|---|---|
| `LocalTime.of(23, 30).plusHours(2)` | `01:30` (wraps, no date to carry into) |
| `LocalDateTime.of(2026, 3, 31, 23, 30).plusHours(2)` | `2026-04-01T01:30` |
| `LocalDate.of(2026, 3, 31).minusMonths(1)` | `2026-02-28` |
| `LocalDate.of(2028, 2, 29).plusYears(1)` | `2029-02-28` |
| `YearMonth.of(2028, 2).atEndOfMonth()` | `2028-02-29` |
| `LocalDate.of(2026, 1, 31).datesUntil(LocalDate.of(2026, 5, 1), Period.ofMonths(1))` | `2026-01-31, 2026-02-28, 2026-03-31, 2026-04-30` (each step from the start) |
| `LocalDate.of(2026, 3, 15).with(TemporalAdjusters.lastInMonth(DayOfWeek.FRIDAY))` | `2026-03-27` |
| `LocalTime.parse("24:00")` | `DateTimeParseException` (hour 0–23; `LocalTime.MAX` is `23:59:59.999999999`) |
| `LocalDateTime.parse("2026-03-27T09:00Z")` | `DateTimeParseException`, unparsed text at index 16 |

## Challenges

1. A rent contract starts on 30 January. Should the March rent be due on the 28th, the 30th or
   the 31st? Write down the rule your schedule follows and the test that pins it.
2. A table has a `LocalDateTime` column written by servers in three zones for five years. What
   would you need to know to turn it into instants, and which rows can't be recovered?
3. A shop is open from 22:00 to 02:00. Which types hold the opening hours, and how do you decide
   whether a given moment falls inside them?

## Design Note: Why plusMonths Clamps Instead of Failing

`plusMonths` could have thrown on 31 January plus one month, as `withDayOfMonth(31)` does in
April. The JDK chose the month's last valid day, as its javadoc says. That suits the common
case, where "a month later" means "the same day if it exists, otherwise the month's end", and no
caller has to handle an exception for February.

A clamped date doesn't record that it was clamped, so month steps don't add up or reverse, and
the bug is a valid, wrong date rather than an exception. A series must keep its first date and
its own end-of-month rule, since a `LocalDate` holds neither.

## Code Examples

- Test: [LocalDateTimeExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datetimeapi/LocalDateTimeExamplesTest.java)
- Source: [LocalDateTimeExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/datetimeapi/LocalDateTimeExamples.java)
