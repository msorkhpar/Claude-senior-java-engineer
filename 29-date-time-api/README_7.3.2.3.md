# 7.3.2.3. Duration and Period

## Concept Explanation

`Duration` and `Period` represent amounts of time, but they model fundamentally different concepts:

- **`Duration`** measures a time-based amount in seconds and nanoseconds. It represents an exact, fixed amount of elapsed time (e.g., "2 hours 30 minutes" = exactly 9000 seconds). It is used with `Instant`, `LocalTime`, and `LocalDateTime`.

- **`Period`** measures a date-based amount in years, months, and days. It represents a calendar-based amount that depends on the calendar context (e.g., "1 month" could be 28, 29, 30, or 31 days depending on which month). It is used with `LocalDate` and `LocalDateTime`.

**Real-world analogy**: `Duration` is like a stopwatch -- it measures exact elapsed time regardless of the calendar. `Period` is like saying "come back in one month" -- the exact number of days varies depending on which month you start from. If you say "come back in 1 month" starting February 1, that is 29 days (in a leap year), but starting March 1, it is 31 days. Same period, different durations.

## Key Points to Remember

1. **Duration is seconds-based** -- internally stored as seconds + nanoseconds.
2. **Period is date-based** -- stored as separate year, month, and day components.
3. **They are NOT interchangeable** -- you cannot add a `Duration` to a `LocalDate` or a `Period` to a `LocalTime`.
4. **Period components are independent** -- `Period.of(1, 15, 3)` stores 1 year, 15 months, and 3 days separately. Call `normalized()` to convert excess months to years.
5. **Duration can be negative** -- represents a backward time span.
6. **Period.between()** gives a signed result (negative if end is before start).
7. **ISO-8601 format**: Duration uses `PTxHxMxS` (e.g., "PT2H30M"); Period uses `PxYxMxD` (e.g., "P1Y2M3D").
8. **`ChronoUnit`** provides an alternative for measuring exact elapsed time in specific units.

## Relevant Java 21 Features

- Duration and Period have been stable since Java 8 with no changes through Java 21.
- `Duration.toXxxPart()` methods (Java 9+) extract individual components: `toHoursPart()`, `toMinutesPart()`, `toSecondsPart()`.
- Streams and functional patterns work naturally with Duration for aggregation (e.g., summing durations).

## Common Pitfalls and How to Avoid Them

1. **Confusing Duration and Period**:
   ```java
   // Problem: Using Duration for date-based arithmetic
   LocalDate date = LocalDate.of(2024, 1, 15);
   date.plus(Duration.ofDays(30)); // UnsupportedTemporalTypeException!

   // Fix: Use Period for date arithmetic
   date.plus(Period.ofMonths(1)); // Correct -- adds 1 calendar month
   ```

2. **Period normalization confusion**:
   ```java
   // Problem: Expecting Period to auto-normalize
   Period p = Period.of(0, 14, 0);
   p.getYears();  // 0 (not 1!)
   p.getMonths(); // 14 (not 2!)

   // Fix: Call normalized()
   Period normalized = p.normalized();
   normalized.getYears();  // 1
   normalized.getMonths(); // 2
   ```

3. **Period does not normalize days**:
   ```java
   // Days are NEVER normalized to months (because days-per-month varies)
   Period p = Period.of(0, 0, 45);
   Period normalized = p.normalized();
   normalized.getDays(); // Still 45 -- days are not converted to months
   ```

4. **Duration.between() requires compatible types**:
   ```java
   // Problem: Can't compute Duration between two LocalDates
   Duration.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1));
   // UnsupportedTemporalTypeException!

   // Fix: Use Period.between() or ChronoUnit.DAYS.between()
   Period.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1));
   ChronoUnit.DAYS.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1));
   ```

## Best Practices and Optimization Techniques

1. **Use `Duration` for measuring elapsed time** (stopwatch-like measurements).
2. **Use `Period` for calendar arithmetic** (adding months, years to dates).
3. **Use `ChronoUnit.between()`** when you need the total in a single unit (e.g., total days between two dates).
4. **Parse ISO-8601 strings** with `Duration.parse()` and `Period.parse()` for config/API values.
5. **Prefer `Duration` over raw milliseconds** for clearer, type-safe code.

## Edge Cases and Their Handling

1. **Zero duration and period**: `Duration.ZERO` and `Period.ZERO` are constants for identity operations.
2. **Negative Duration**: `Duration.between(later, earlier)` returns a negative duration.
3. **Negative Period**: `Period.between(laterDate, earlierDate)` returns negative components.
4. **Adding Period across month boundaries**: `LocalDate.of(2024, 1, 31).plus(Period.ofMonths(1))` returns Feb 29 (clamped).
5. **Large durations**: `Duration.ofDays(365)` is exactly 365 * 86400 seconds, ignoring leap seconds and DST.

## Interview-specific Insights

Interviewers probe:
- Understanding the fundamental difference between Duration and Period
- Knowledge of when each is appropriate
- Awareness of normalization behavior in Period
- Edge cases around month-end clamping
- ISO-8601 duration/period format

## Interview Q&A Section

**Q1: What is the fundamental difference between `Duration` and `Period`?**

```text
A1: The fundamental difference is what they measure:

Duration: Time-based (exact seconds + nanoseconds)
- Always the same length regardless of context
- "2 hours" is always 7200 seconds
- Used with: Instant, LocalTime, LocalDateTime
- Suitable for: timeouts, elapsed time, benchmarks

Period: Date-based (years, months, days)
- Length depends on calendar context
- "1 month" could be 28, 29, 30, or 31 days
- Used with: LocalDate, LocalDateTime
- Suitable for: subscriptions, age calculation, billing cycles

Key insight: "30 days" (Duration) and "1 month" (Period) are NOT the same thing.
If you start on January 15 and add 1 month, you get February 15.
If you add 30 days, you get February 14.
```

```java
// Duration: exact time-based amount
Duration twoHours = Duration.ofHours(2);          // Exactly 7200 seconds
Duration halfDay = Duration.ofHours(12);           // Exactly 43200 seconds
Duration timeout = Duration.ofMillis(500);          // Exactly 500 ms

// Period: calendar-based amount (context-dependent)
Period oneMonth = Period.ofMonths(1);               // 28-31 days depending on month
Period oneYear = Period.ofYears(1);                  // 365 or 366 days
Period subscription = Period.of(1, 6, 0);            // 1 year 6 months

// Same "30 days" vs "1 month" can give different results
LocalDate start = LocalDate.of(2024, 1, 15);
LocalDate plus30Days = start.plusDays(30);            // 2024-02-14
LocalDate plus1Month = start.plus(Period.ofMonths(1)); // 2024-02-15
```

**Q2: How does `Period.normalized()` work?**

```text
A2: Period.normalized() converts excess months to years, keeping the relationship
years * 12 + months constant, without touching the days component.

Rules:
- Months >= 12 are converted to years: Period.of(0, 15, 10).normalized() = P1Y3M10D
- Negative months with positive years (or vice versa) are balanced
- Days are NEVER normalized because the number of days per month varies
- The signs of years and months are normalized to be consistent

Example: Period.of(1, -2, 5).normalized() = P0Y10M5D (10 months instead of 1 year - 2 months)
```

```java
// Months to years
Period p1 = Period.of(0, 15, 10);
Period n1 = p1.normalized();
// n1 = P1Y3M10D (15 months = 1 year + 3 months, days unchanged)

// Mixed signs
Period p2 = Period.of(2, -3, 0);
Period n2 = p2.normalized();
// n2 = P1Y9M (2 years - 3 months = 1 year 9 months)

// Days are never normalized
Period p3 = Period.of(0, 0, 45);
Period n3 = p3.normalized();
// n3 = P45D (still 45 days -- not converted to months)
```

**Q3: How do you calculate the total number of days in a `Period`?**

```text
A3: You cannot directly get total days from a Period because months have variable 
lengths. Instead, use ChronoUnit.DAYS.between() on the actual dates.

Period.getDays() only returns the days COMPONENT, not the total:
Period.of(1, 2, 3).getDays() = 3 (not ~430!)

For total days, you must know the start date:
```

```java
Period period = Period.of(1, 2, 3);

// WRONG: This only gets the days component (3)
int daysComponent = period.getDays(); // 3

// CORRECT: Calculate from actual dates
LocalDate start = LocalDate.of(2024, 1, 1);
LocalDate end = start.plus(period);
long totalDays = ChronoUnit.DAYS.between(start, end); // 429

// Different start date gives different total days
LocalDate start2 = LocalDate.of(2024, 3, 1);
LocalDate end2 = start2.plus(period);
long totalDays2 = ChronoUnit.DAYS.between(start2, end2); // 430 (different!)
```

**Q4: How do you parse and format Duration and Period from strings?**

```text
A4: Both Duration and Period use ISO-8601 format for their toString() and parse():

Duration format: PT[nH][nM][nS]
- PT2H30M = 2 hours 30 minutes
- PT45S = 45 seconds
- PT1H30M15.5S = 1 hour 30 minutes 15.5 seconds
- PT-2H = negative 2 hours

Period format: P[nY][nM][nD]
- P1Y2M3D = 1 year 2 months 3 days
- P6M = 6 months
- P-1Y = negative 1 year
- P0D = zero period

These are commonly used in configuration files, API responses, and databases.
```

```java
// Duration parsing
Duration d1 = Duration.parse("PT2H30M");     // 2 hours 30 minutes
Duration d2 = Duration.parse("PT45S");        // 45 seconds
Duration d3 = Duration.parse("PT1H30M15.5S"); // 1h 30m 15.5s

// Duration formatting
String formatted = Duration.ofHours(2).plusMinutes(30).toString(); // "PT2H30M"

// Period parsing
Period p1 = Period.parse("P1Y2M3D");   // 1 year 2 months 3 days
Period p2 = Period.parse("P6M");       // 6 months
Period p3 = Period.parse("P10D");      // 10 days

// Period formatting
String pFormatted = Period.of(1, 6, 15).toString(); // "P1Y6M15D"
```

**Q5: What happens when you add a `Duration` of days to a `ZonedDateTime` across a DST boundary?**

```text
A5: This is a subtle but important distinction:

Duration.ofDays(1) adds exactly 24 hours (86400 seconds).
Period.ofDays(1) adds one calendar day (same wall-clock time next day).

Across a DST boundary, these give different results:

On spring-forward day (US Eastern, March 10, 2024):
- Duration.ofDays(1): 24 hours later = same instant + 24h, but local time shifts
- Period.ofDays(1): same local time next day = 23 actual hours elapsed

On fall-back day:
- Duration.ofDays(1): 24 hours later (wall clock shows +25 hours due to repeated hour)
- Period.ofDays(1): same local time (25 actual hours elapsed)

This is why choosing between Duration and Period matters for scheduling.
```

```java
ZoneId ny = ZoneId.of("America/New_York");
// March 10, 2024: clocks spring forward at 2 AM

ZonedDateTime beforeDST = ZonedDateTime.of(2024, 3, 10, 0, 0, 0, 0, ny);

// Duration: exactly 24 hours
ZonedDateTime plusDuration = beforeDST.plus(Duration.ofDays(1));
// 2024-03-11T01:00 (moved 24 hours, but wall clock shows 1 AM due to DST)

// Period: same wall clock time
ZonedDateTime plusPeriod = beforeDST.plus(Period.ofDays(1));
// 2024-03-11T00:00 (same midnight, but only 23 hours actually elapsed)
```

## Code Examples

- Test: [DurationPeriodExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datetimeapi/DurationPeriodExamplesTest.java)
- Source: [DurationPeriodExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/datetimeapi/DurationPeriodExamples.java)
