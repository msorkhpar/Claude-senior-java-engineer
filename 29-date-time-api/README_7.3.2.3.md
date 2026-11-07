# 7.3.2.3. Duration and Period

A shop chain's daily sales report is due at 07:00, Amsterdam time. From 29 March 2026 it arrives
at 08:00, every day. On 25 October it is back at 07:00, and nobody touched it. The scheduler works
out the next run like this:

```java
ZonedDateTime next = last.plus(Duration.ofDays(1));
```

`java.time` has two types for an amount of time. `Duration` is an exact number of seconds and
nanoseconds. `Period` is a number of years, months and days on a calendar. Outputs below are from
Java 21.0.12 with tzdata 2026b, run with `-Duser.timezone=UTC`; a run with
`-Duser.timezone=Asia/Tokyo` printed the same lines, since nothing here reads the default zone.

## Duration.ofDays(1) Is 24 Hours; Period.ofDays(1) Is the Next Calendar Day

Add each kind of day to a 07:00 run on the day before each clock change. What does the wall
clock show?

```java
ZoneId amsterdam = ZoneId.of("Europe/Amsterdam");
for (LocalDate day : List.of(LocalDate.of(2026, 3, 28), LocalDate.of(2026, 10, 24))) {
    ZonedDateTime report = ZonedDateTime.of(day, LocalTime.of(7, 0), amsterdam);
    ZonedDateTime byDuration = report.plus(Duration.ofDays(1));
    ZonedDateTime byPeriod = report.plus(Period.ofDays(1));
    System.out.println(report);
    System.out.println("  + Duration.ofDays(1) = " + byDuration);
    System.out.println("  + Period.ofDays(1)   = " + byPeriod
            + ", " + Duration.between(report, byPeriod).toHours() + " hours later");
}
```

```text
2026-03-28T07:00+01:00[Europe/Amsterdam]
  + Duration.ofDays(1) = 2026-03-29T08:00+02:00[Europe/Amsterdam]
  + Period.ofDays(1)   = 2026-03-29T07:00+02:00[Europe/Amsterdam], 23 hours later
2026-10-24T07:00+02:00[Europe/Amsterdam]
  + Duration.ofDays(1) = 2026-10-25T06:00+01:00[Europe/Amsterdam]
  + Period.ofDays(1)   = 2026-10-25T07:00+01:00[Europe/Amsterdam], 25 hours later
```

`Duration.ofDays(1)` is 86,400 seconds; "days" is only a way to write them. Added to a
`ZonedDateTime`, it moves the instant on by that much. 29 March has 23 hours in Amsterdam, so 24
hours after 07:00 on the 28th the clock reads 08:00, and every later run adds 24 hours to 08:00 until
October's 25-hour day takes the hour back. `Period.ofDays(1)` changes the date and keeps the
wall-clock time.

A report "every day at 07:00" and a renewal "in one month" are calendar amounts: use `Period` or
`plusDays`. A session that expires "after 24 hours" and a timeout are elapsed time: use
`Duration`. Mixed up the other way, a 24-hour reservation made with `plusDays(1)` lasts 25 hours
one night a year and 23 another. A `LocalDate` has no seconds, so adding a `Duration` to it throws
(see the Reference). Times inside the gap and the overlap themselves are unit 7.3.2.5.

<!-- practice 1 -->

## Period.between Returns Fields; Count Days With ChronoUnit.DAYS.between

An invoice was due on 31 January 2026, and the late fee is charged per day. The billing code reads
the number of days late from `Period.between(due, paid).getDays()`:

```java
LocalDate due = LocalDate.of(2026, 1, 31);
for (LocalDate paid : List.of(LocalDate.of(2026, 2, 3), LocalDate.of(2026, 3, 3), LocalDate.of(2027, 1, 30))) {
    Period p = Period.between(due, paid);
    System.out.println("paid " + paid + ": Period " + p + ", getDays() " + p.getDays()
            + ", DAYS.between " + ChronoUnit.DAYS.between(due, paid));
}
```

```text
paid 2026-02-03: Period P3D, getDays() 3, DAYS.between 3
paid 2026-03-03: Period P1M3D, getDays() 3, DAYS.between 31
paid 2027-01-30: Period P11M30D, getDays() 30, DAYS.between 364
```

`Period.between` answers "how many whole years, months and days". `getDays()` is the last field of that answer, the days left over after the whole
months. The customer who paid 31 days late was charged for 3, and the one who paid 364 days late
for 30. Every test invoice had been paid within the month.

For a total in one unit, use `ChronoUnit.DAYS.between(a, b)` (or `WEEKS`, `MONTHS`, ...), or
`a.until(b, unit)`. Keep `Period.between` for answers in calendar fields: an age is
`Period.between(birth, today).getYears()`, which a count of days divided by 365 gets wrong in the
days before a birthday.

<!-- practice 2 -->

## A Period Keeps the Fields It Was Given, So P14M Is Not P1Y2M

A plan catalogue finds plans by their term. One supplier sends `P14M`, and the catalogue holds
`P1Y2M`:

```java
Period fourteen = Period.ofMonths(14);
Period yearTwo = Period.of(1, 2, 0);
System.out.println(fourteen + " equals " + yearTwo + ": " + fourteen.equals(yearTwo));
System.out.println(fourteen + ".normalized() = " + fourteen.normalized()
        + ", equals: " + fourteen.normalized().equals(yearTwo));
System.out.println(Period.ofDays(45) + ".normalized() = " + Period.ofDays(45).normalized());
System.out.println(Period.of(1, -3, 0) + ".normalized() = " + Period.of(1, -3, 0).normalized());
```

```text
P14M equals P1Y2M: false
P14M.normalized() = P1Y2M, equals: true
P45D.normalized() = P45D
P1Y-3M.normalized() = P9M
```

A `Period` stores its three fields as given, and `equals` compares the fields. `normalized()`
folds months into years, twelve to one, and balances their signs. It never touches the days: how
many days make a month depends on the month, and a `Period` doesn't know which one it will be
added to. So `P1M` and `P30D` stay different: from 1 February they give
different dates. Normalize both sides before you compare two terms or use one as a map key.

`Duration` stores an exact length, so it has no such question: `Duration.ofMinutes(90)` and
`Duration.ofSeconds(5400)` are equal and both print `PT1H30M`.

<!-- practice 4 -->

## Duration Prints Hours, Not Days, and Its Hours and Minutes Carry the Sign

`toString()` and `parse()` use ISO-8601: `P`, then date fields, then `T` and time fields.
Expression, then result:

```text
Duration.ofMinutes(90)       PT1H30M
Duration.ofDays(2)           PT48H
Duration.parse("P2DT3H")     PT51H
Duration.ofMillis(1500)      PT1.5S
Period.of(1, 2, 3)           P1Y2M3D
Period.ofWeeks(2)            P14D
Duration.parse("PT1M")       PT1M
Duration.parse("P1M")        DateTimeParseException: Text cannot be parsed to a Duration
```

A `Duration` never prints days: its largest field is the hour. It accepts `D` on the way in and
counts each day as 24 hours, but it refuses years, months and weeks. A config value `P1M` is a month to
`Period.parse` and an error to `Duration.parse`; `PT1M` is one minute.

A night shift runs from 22:00 to 06:30, and a timesheet shows an overtime balance of minus 90
minutes:

```java
Duration shift = Duration.between(LocalTime.of(22, 0), LocalTime.of(6, 30));
System.out.println("shift: " + shift);
Duration balance = Duration.ofMinutes(-90);
System.out.printf("balance: %d:%02d%n", balance.toHours(), balance.toMinutesPart());
Duration abs = balance.abs();
System.out.printf("balance: %s%d:%02d%n", balance.isNegative() ? "-" : "", abs.toHours(), abs.toMinutesPart());
```

```text
shift: PT-15H-30M
balance: -1:-30
balance: -1:30
```

`between(start, end)` subtracts, and a `LocalTime` has no next day, so the shift came out as
minus 15½ hours. Where an end before the start means the next day, add `Duration.ofDays(1)` to a
negative result. In a negative `Duration`, `toHours()` and `toMinutesPart()` (and
`toSecondsPart()`) are all negative, so the first balance printed `-1:-30`. Print the sign once
and the parts of `abs()`. `Period.between` with the later date first gives `P-1M-3D`, every field
negative.

`toHours()` is the total; `toHoursPart()` is the hours left after whole days. A timesheet
total of 30 hours printed with `toHoursPart()` shows 6.

<!-- practice 3 -->

## Summary

- **`Duration.ofDays(1)` is 24 hours, `Period.ofDays(1)` is the next calendar day.** Across a DST
  change they differ by an hour: schedules use `Period`, expiries and timeouts `Duration`.
- **`Period.between(...).getDays()` is a field, not a total.** Count with
  `ChronoUnit.DAYS.between`; use `Period.between` for ages and other calendar answers.
- **A `Period` compares field by field.** `P14M` is not `P1Y2M` until normalized, and
  `normalized()` never turns days into months.
- **A `Duration` prints hours, not days, and its hours and minutes carry its sign.** `between` doesn't
  wrap midnight; format a negative amount from its `abs()`.

## Reference

From this unit's runs (Java 21):

| Expression | Result |
|---|---|
| `LocalDate.of(2026, 3, 28).plus(Duration.ofDays(1))` | `UnsupportedTemporalTypeException: Unsupported unit: Seconds` |
| `Duration.between(LocalDate, LocalDate)` | `UnsupportedTemporalTypeException: Unsupported unit: Seconds` |
| `LocalTime.of(9, 0).plus(Period.ofDays(1))` | `UnsupportedTemporalTypeException: Unsupported unit: Days` |
| `HOURS.between(a, a.plusDays(1))`, `a` = Amsterdam 2026-03-29T00:00 | `23` (`DAYS.between` gives `1`; between the two `LocalDateTime`s, `HOURS` gives `24`) |
| `Duration.ofHours(30)`: `toDays()`, `toDaysPart()`, `toHoursPart()` | `1`, `1`, `6` |
| `Duration.ZERO`, `Period.ZERO` | `PT0S`, `P0D` |
| `Period.of(1, 2, 3).toTotalMonths()` | `14` |
| `Period.parse("P12M")` | `P12M` (parse doesn't normalize) |
| `Period.parse("P1Y2M3W4D")` | `P1Y2M25D` (weeks become days) |
| `Period.parse("PT1H")` | `DateTimeParseException` |
| `Period.of(1, -3, 0).isNegative()` | `true` (any negative field; normalized it is `P9M`) |
| `Period.between(2028-02-29, 2029-02-28)` | `P11M30D` |

## Challenges

1. A gym membership runs for one month and can be paused for 10 days. Is the pause a `Period`
   or a `Duration`, and what decides it?
2. An API takes a retention setting as ISO-8601 text. Which strings should it accept, and how do
   you catch a user who wrote `P1M` meaning one minute?
3. Find the places in a codebase you know that turn a `Duration` into text for a user. Which of
   them break for a negative amount or for more than 24 hours?

## Design Note: Why Two Amount Types

An amount that held "1 month and 3 hours" would need a date before it had a length, so it could
not be compared, summed or stored as a number. `java.time` splits it in two. `Duration` is a
number: it sorts, adds and divides, and it is the same length everywhere. `Period` is an
instruction to the calendar: it means what people mean by "next month", and it has no length until
it meets a date. `Duration.ofDays` looks like a calendar amount and isn't; that is the opening
scheduler's bug.

Every caller has to decide which question each amount answers, because `plus(TemporalAmount)`
accepts both, and a `LocalDate` refuses a `Duration` only at run time.

## Code Examples

- Test: [DurationPeriodExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datetimeapi/DurationPeriodExamplesTest.java)
- Source: [DurationPeriodExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/datetimeapi/DurationPeriodExamples.java)
