# 7.3.2.4. DateTimeFormatter

On Monday 29 December 2025 the nightly settlement job uploads `settlement-2026-12-29.csv`, and the
bank rejects it as dated a year ahead. The job ran fine all year, and its formatter is a
constant nobody has touched in years.

Outputs are from Java 21.0.12 and its bundled CLDR locale data, run with `-Duser.timezone=UTC
-Duser.language=en -Duser.country=US`; Java 25.0.4 printed the same.

## Pattern Letters Are Case-Sensitive, and YYYY Is the Year of the Week

The settlement job formats with `YYYY-MM-dd` and `Locale.UK`. Beside `uuuu`, around two New Years:

```java
DateTimeFormatter calendarYear = DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.US);
DateTimeFormatter weekYearUS = DateTimeFormatter.ofPattern("YYYY-MM-dd", Locale.US);
DateTimeFormatter weekYearUK = DateTimeFormatter.ofPattern("YYYY-MM-dd", Locale.UK);
for (LocalDate day : List.of(LocalDate.of(2025, 12, 27), LocalDate.of(2025, 12, 28),
                             LocalDate.of(2025, 12, 29), LocalDate.of(2027, 1, 1))) {
    System.out.println(calendarYear.format(day) + "   YYYY en-US: " + weekYearUS.format(day)
            + "   YYYY en-GB: " + weekYearUK.format(day));
}
```

```text
2025-12-27   YYYY en-US: 2025-12-27   YYYY en-GB: 2025-12-27
2025-12-28   YYYY en-US: 2026-12-28   YYYY en-GB: 2025-12-28
2025-12-29   YYYY en-US: 2026-12-29   YYYY en-GB: 2026-12-29
2027-01-01   YYYY en-US: 2027-01-01   YYYY en-GB: 2026-01-01
```

`Y` is the **week-based year**: the year that owns the day's week, and the formatter's locale
defines the weeks. US weeks start on Sunday, and the week holding 1 January belongs to the new
year, so from Sunday 28 December 2025 it prints 2026. UK weeks follow ISO-8601: they start on
Monday and belong to the year holding at least four of their days. So 1 January 2027, a Friday,
prints 2026, and that night's file overwrites the one from 1 January 2026. The rest of the year `Y` and `y`
agree, which is why the tests pass.

`y` (year-of-era) and `u` (proleptic year) differ only before year 1 and under the strict
resolver of the next section. Case changes other letters too, at 14:05 on 29 December:

```text
uuuu-mm-dd  2025-05-29
hh:mm  02:05
HH:mm  14:05
hh:mm a  02:05 PM
```

`mm` is the minute, so the date gets month 05. `hh` is the 12-hour clock: 02:05 and 14:05 print
alike, and logs sorted by that stamp interleave night and afternoon. Use `HH`, or `hh` with `a`.

<!-- practice 1 -->

## ofPattern Turns 30 February Into the 28th Unless the Resolver Is STRICT, and STRICT Needs uuuu

A booking form reads the date a guest typed:

```java
formatters.put("uuuu-MM-dd        ", DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.ROOT));
formatters.put("uuuu-MM-dd, STRICT", DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.ROOT)
        .withResolverStyle(ResolverStyle.STRICT));
formatters.put("yyyy-MM-dd, STRICT", DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT)
        .withResolverStyle(ResolverStyle.STRICT));
```

```text
uuuu-MM-dd          2026-02-28 -> 2026-02-28
uuuu-MM-dd          2026-02-30 -> 2026-02-28
uuuu-MM-dd, STRICT  2026-02-28 -> 2026-02-28
uuuu-MM-dd, STRICT  2026-02-30 -> Text '2026-02-30' could not be parsed: Invalid date 'FEBRUARY 30'
yyyy-MM-dd, STRICT  2026-02-28 -> Text '2026-02-28' could not be parsed: Unable to obtain LocalDate from TemporalAccessor: {MonthOfYear=2, DayOfMonth=28, YearOfEra=2026},ISO of type java.time.format.Parsed
yyyy-MM-dd, STRICT  2026-02-30 -> Text '2026-02-30' could not be parsed: Unable to obtain LocalDate from TemporalAccessor: {MonthOfYear=2, DayOfMonth=30, YearOfEra=2026},ISO of type java.time.format.Parsed
```

`ofPattern` resolves in `ResolverStyle.SMART`, which the javadoc describes as "converting any value
beyond the last valid day-of-month to be the last valid day-of-month". So a guest who typed 31
April is silently booked for 30 April. Day 32 still throws: SMART checks each field's range, not
whether the combination exists. The `ISO_*` constants and `LocalDate.parse(text)` (unit 7.3.2.1)
are already STRICT.

STRICT with `yyyy` and no `G` refuses every date: a year-of-era means nothing without an era (AD or BC), and
STRICT won't assume one. Use `uuuu`, which needs no era.

When parsing, `dd` demands two digits (`7.3.2026` fails at index 0); `d` also accepts `7`.

<!-- practice 2 -->

## Month and Day Names Come From a Locale, So Name One

A formatter built without a `Locale` takes the JVM's default. The same line in two JVMs, the first
started with `-Duser.language=en -Duser.country=US`, the second with `-Duser.language=de
-Duser.country=DE`:

```java
DateTimeFormatter header = DateTimeFormatter.ofPattern("EEE d MMM uuuu");   // no Locale
System.out.println(header.getLocale() + ": " + header.format(LocalDate.of(2026, 9, 7)));
```

```text
en_US: Mon 7 Sep 2026
de_DE: Mo. 7 Sept. 2026
```

A container image that changes its `LANG` changes every report header. A partner's file writes `07-Sep-2026`; four locales format and parse it with
`dd-MMM-uuuu`:

```text
en-US prints 07-Sep-2026   reads 07-Sep-2026: 2026-09-07
en-GB prints 07-Sept-2026  reads 07-Sep-2026: Text '07-Sep-2026' could not be parsed at index 3
de-DE prints 07-Sept.-2026 reads 07-Sep-2026: Text '07-Sep-2026' could not be parsed at index 3
und   prints 07-Sep-2026   reads 07-Sep-2026: 2026-09-07
```

The names come from CLDR data, which differs by locale and can change between JDK releases. Pick
the locale whose names match the text, here `Locale.US`. `Locale.ROOT` (`und`) suits text made of
digits, but it has no full names: `EEEE d MMMM uuuu` prints `Mon 7 Sep 2026`. Parsing names is also
case-sensitive, so `07-SEP-2026` fails at index 3 even with `Locale.US`; a formatter from
`new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(...)` accepts it.

A formatter is immutable, so one `static final` instance is safe for every thread, unlike a shared
`SimpleDateFormat` (unit 7.3.1). `withLocale` and `withZone` return a new formatter, and dropping
the result changes nothing:

```java
DateTimeFormatter receipt = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.US);
receipt.withLocale(Locale.FRANCE);                           // result discarded
System.out.println(receipt.format(LocalDate.of(2026, 9, 7)));
System.out.println(receipt.withLocale(Locale.FRANCE).format(LocalDate.of(2026, 9, 7)));
```

```text
7 September 2026
7 septembre 2026
```

<!-- practice 3 -->

## An Offset in the Text Is Part of the Value, So Parse Into a Type That Keeps It

A partner sends event times with an offset. These two name the same moment:

```java
DateTimeFormatter xxx = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssxxx", Locale.ROOT);
for (String text : List.of("2026-03-14T10:30:00+01:00", "2026-03-14T09:30:00Z")) {
    LocalDateTime dropped = LocalDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    Instant kept = OffsetDateTime.parse(text).toInstant();
    // ... and OffsetDateTime.parse(text, xxx), printed or its exception message
}
```

```text
2026-03-14T10:30:00+01:00
  as LocalDateTime:  2026-03-14T10:30
  as Instant:        2026-03-14T09:30:00Z
  pattern ...xxx:    2026-03-14T10:30+01:00
2026-03-14T09:30:00Z
  as LocalDateTime:  2026-03-14T09:30
  as Instant:        2026-03-14T09:30:00Z
  pattern ...xxx:    Text '2026-03-14T09:30:00Z' could not be parsed at index 19
```

`LocalDateTime` has no field for the offset, so the parse dropped it without an error. Stored as UTC, every event from the `+01:00` sender lands an hour late.
`OffsetDateTime`, `ZonedDateTime` and `Instant` keep the moment.

The hand-written pattern fails the other sender: `xxx` reads `+00:00` but never `Z`. `XXX`
accepts both, and so does `ISO_OFFSET_DATE_TIME`. For text that machines exchange, use the `ISO_*`
constants in the Reference; write patterns for text people read.

An `Instant` has no year or hour until a zone is chosen (unit 7.3.2.2), so a pattern formatter
can't print one until it gets `withZone`:

```text
no zone:   Unsupported field: Year
Amsterdam: 2026-03-14 10:30
```

Zones and DST belong to unit 7.3.2.5.

<!-- practice 4 -->

<!-- practice 5 -->

## Summary

- **`YYYY` is the week-based year.** It prints next year in late December, on days the locale's
  week rules pick; pattern letters are case-sensitive (`MM`/`mm`, `HH`/`hh`).
- **`ofPattern` resolves SMART** and moves 30 February to the 28th. `STRICT` refuses it, and needs
  `uuuu`.
- **Names come from the formatter's locale**, the JVM default if none is given; en-US and en-GB
  disagree (`Sep`, `Sept`).
- **A formatter is immutable**: share one constant, keep what `withLocale` returns.
- **Parse offset text into a type that keeps the offset.** `LocalDateTime` drops it silently.

## Reference

**Letters that get confused (javadoc of `DateTimeFormatter`, checked on Java 21):**

| Letter | Meaning | Not to be confused with |
|---|---|---|
| `u` | year; year 0 prints `0000` | `y` year-of-era; year 0 prints `0001` (era `BC`) |
| `Y` | week-based year, weeks from the locale | `u`, `y` |
| `M` | month | `m` minute |
| `H` | hour 0 to 23 | `h` hour 1 to 12, needs `a` |
| `XXX` | offset, `Z` for zero | `xxx`: `+00:00` for zero; `Z`: `+0100`, `+0000` |
| `VV` | zone id (`Europe/Amsterdam`) | `z` zone name from the locale (`CET` en-US, `MEZ` de-DE) |

**Constants, formatting 2026-03-14T10:30 in Europe/Amsterdam** (all STRICT except
`RFC_1123_DATE_TIME`, which is SMART and prints English names under any locale):

| Constant | Output |
|---|---|
| `ISO_LOCAL_DATE` | `2026-03-14` |
| `ISO_LOCAL_DATE_TIME` | `2026-03-14T10:30:00` |
| `ISO_OFFSET_DATE_TIME` | `2026-03-14T10:30:00+01:00` |
| `ISO_ZONED_DATE_TIME` | `2026-03-14T10:30:00+01:00[Europe/Amsterdam]` |
| `ISO_INSTANT` | `2026-03-14T09:30:00Z` |
| `RFC_1123_DATE_TIME` | `Sat, 14 Mar 2026 10:30:00 +0100` |

**Edge cases (Java 21):** `parse(null, f)` throws `NullPointerException`; `[...]` marks an
optional section, so `uuuu-MM-dd['T'HH:mm]` reads a date with or without a time.

## Challenges

1. A service prints dates for users in 30 locales and reads three partners' files. Where does
   each formatter's locale come from?
2. How would your tests notice a JDK upgrade that changes the names a locale prints?
3. In a codebase you know, how many `ofPattern` calls name a locale, and how many use `YYYY` or
   `hh` without `a`?

## Design Note: Why the Rules Live Inside the Formatter

`SimpleDateFormat` changed its own `Calendar` on every call. A `DateTimeFormatter` holds only a
description fixed when it is built (pattern, locale, resolver style, optional zone). The cost is that the settings deciding the output sit where the formatter is
built, not where it is used: a pattern read in review shows nothing of the locale behind `Y`, `MMM`
or `a`, or of SMART resolution. Name them at the build site.

## Code Examples

- Test: [DateTimeFormatterExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datetimeapi/DateTimeFormatterExamplesTest.java)
- Source: [DateTimeFormatterExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/datetimeapi/DateTimeFormatterExamples.java)
