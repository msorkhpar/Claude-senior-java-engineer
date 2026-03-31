# 7.3.2.1. LocalDate, LocalTime, and LocalDateTime

## Concept Explanation

`LocalDate`, `LocalTime`, and `LocalDateTime` are the fundamental "local" temporal types in the `java.time` package. The word "local" means they carry no timezone information -- they represent a date, a time, or both as seen on a wall clock or calendar, without any reference to a specific location on Earth.

**Real-world analogy**: Think of these as what you see on a physical desk calendar (`LocalDate`), a kitchen wall clock (`LocalTime`), or both combined in a paper planner (`LocalDateTime`). None of these physical objects know about timezones -- they just show numbers. The timezone context is implicit (wherever the calendar or clock is physically located).

- `LocalDate` stores year, month, and day (e.g., 2024-03-15).
- `LocalTime` stores hour, minute, second, and nanosecond (e.g., 14:30:45.123456789).
- `LocalDateTime` combines both into a single object (e.g., 2024-03-15T14:30:45).

All three are immutable, thread-safe, and follow ISO-8601 conventions. They use the ISO chronology (Gregorian calendar).

## Key Points to Remember

1. **No timezone** -- these types are timezone-agnostic. `LocalDateTime` is NOT a replacement for `ZonedDateTime`.
2. **Immutable** -- all modification methods return new instances.
3. **1-based months** -- January = 1, December = 12 (unlike `Calendar`).
4. **Value-based equality** -- `equals()` compares the actual date/time values.
5. **Strict validation** -- `LocalDate.of(2024, 2, 30)` throws `DateTimeException`.
6. **Nanosecond precision** -- `LocalTime` supports up to nanosecond resolution.
7. **`LocalDate.datesUntil()`** -- Java 9+ method that returns a `Stream<LocalDate>` for date ranges.
8. **`TemporalAdjusters`** -- utility for complex calendar operations like "next Monday" or "last day of month".

## Relevant Java 21 Features

- `LocalDate.datesUntil()` (Java 9+) integrates with streams for date range processing.
- Pattern matching for `switch` can be used with temporal types when combined with accessor methods.
- Records work naturally with `LocalDate` fields for DTOs: `record Event(String name, LocalDate date) {}`.
- Virtual threads benefit from immutability -- no synchronization needed when sharing dates across virtual threads.

## Common Pitfalls and How to Avoid Them

1. **Using `LocalDateTime` for timestamps**:
   ```java
   // Problem: No timezone context -- ambiguous for distributed systems
   LocalDateTime eventTime = LocalDateTime.now();

   // Fix: Use Instant for timestamps, ZonedDateTime for user-facing times
   Instant eventTime = Instant.now();
   ```

2. **Forgetting immutability**:
   ```java
   // Problem: Ignoring the return value
   LocalDate date = LocalDate.of(2024, 3, 15);
   date.plusDays(1); // Return value discarded! date is unchanged

   // Fix: Assign the result
   LocalDate nextDay = date.plusDays(1);
   ```

3. **Month-end arithmetic surprises**:
   ```java
   // Adding months to the 31st
   LocalDate jan31 = LocalDate.of(2024, 1, 31);
   LocalDate feb = jan31.plusMonths(1); // 2024-02-29 (not March 2!)
   LocalDate mar = feb.plusMonths(1);   // 2024-03-29 (not March 31!)
   // Round-tripping through plusMonths may change the day
   ```

4. **Comparing with `==` instead of `equals()`**:
   ```java
   // Problem: Reference comparison
   LocalDate a = LocalDate.of(2024, 3, 15);
   LocalDate b = LocalDate.of(2024, 3, 15);
   a == b; // May be false (no caching guarantee)

   // Fix: Use equals() or compareTo()
   a.equals(b); // true
   ```

## Best Practices and Optimization Techniques

1. **Use `LocalDate` for business logic that doesn't need time** (birthdays, holidays, deadlines).
2. **Use `TemporalAdjusters`** for complex calendar calculations rather than manual arithmetic.
3. **Use `LocalDate.datesUntil()`** for iterating over date ranges instead of manual loops.
4. **Inject `Clock`** for testability instead of calling `LocalDate.now()` directly.
5. **Use `DayOfWeek` and `Month` enums** instead of magic numbers for readable code.

## Edge Cases and Their Handling

1. **Leap year handling**: `LocalDate.of(2024, 2, 29)` is valid; `LocalDate.of(2023, 2, 29)` throws `DateTimeException`.
2. **End-of-month clamping**: `LocalDate.of(2024, 3, 31).plusMonths(1)` returns April 30 (clamped).
3. **Midnight boundary**: `LocalTime.MIDNIGHT` is `00:00:00`; `LocalTime.MAX` is `23:59:59.999999999`.
4. **Overflow in business day calculation**: weekends-only ranges yield zero business days.
5. **Year zero**: `LocalDate` supports year 0 (1 BC in the proleptic Gregorian calendar).

## Interview-specific Insights

Interviewers focus on:
- Understanding the difference between `LocalDateTime` and `ZonedDateTime`
- Knowledge of immutability and its benefits
- Ability to use `TemporalAdjusters` for complex calculations
- Edge cases around month-end arithmetic and leap years
- Choosing the right type for a given scenario

## Interview Q&A Section

**Q1: When should you use `LocalDate` vs `LocalDateTime` vs `ZonedDateTime`?**

```text
A1: Choose based on the semantic meaning of your data:

LocalDate: Use when only the date matters, with no time component.
Examples: birthdays, public holidays, invoice dates, subscription expiry dates.

LocalDateTime: Use when you need both date and time but the timezone is implicit 
(all parties are in the same timezone or timezone is irrelevant).
Examples: local event schedules, appointment times within a single timezone.

ZonedDateTime: Use when timezone context is essential for correctness.
Examples: international meeting scheduling, flight departure times, converting 
between user timezones.

Key rule: If your application serves users in multiple timezones or stores data 
in a distributed system, you almost certainly need Instant (for storage) and 
ZonedDateTime (for display). LocalDateTime is only appropriate when timezone 
is truly irrelevant or universally agreed upon.
```

```java
// Birthday -- only date matters
LocalDate birthday = LocalDate.of(1990, Month.JUNE, 15);

// Doctor appointment -- timezone is implicit (local clinic)
LocalDateTime appointment = LocalDateTime.of(2024, 3, 15, 14, 30);

// International meeting -- timezone is critical
ZonedDateTime meeting = ZonedDateTime.of(2024, 3, 15, 14, 30, 0, 0,
        ZoneId.of("America/New_York"));
```

**Q2: How does month-end arithmetic work in java.time?**

```text
A2: When adding months to a date, if the resulting month has fewer days than the 
original day-of-month, java.time clamps the day to the last valid day of the 
resulting month. This is called "smart resolution" or "day-of-month clamping."

Examples:
- January 31 + 1 month = February 29 (leap year) or February 28 (non-leap)
- March 31 - 1 month = February 29 or 28
- January 31 + 1 month + 1 month ≠ January 31 + 2 months (potentially)

This means month arithmetic is NOT always reversible:
date.plusMonths(1).minusMonths(1) may not equal date.

This is by design -- it preserves the invariant that the result is always a 
valid date, at the cost of mathematical reversibility.
```

```java
LocalDate jan31 = LocalDate.of(2024, 1, 31);

// Clamping in action
LocalDate feb = jan31.plusMonths(1);  // 2024-02-29 (leap year)
LocalDate mar = feb.plusMonths(1);    // 2024-03-29 (not 31!)

// Not reversible
LocalDate backToJan = mar.minusMonths(2); // 2024-01-29 (not 31!)

// Direct 2-month addition gives a different result
LocalDate twoMonths = jan31.plusMonths(2); // 2024-03-31
```

**Q3: How do you calculate business days between two dates?**

```text
A3: java.time doesn't have built-in business day support, but you can easily 
implement it using LocalDate.datesUntil() combined with stream filtering:

1. Generate a stream of dates in the range
2. Filter out weekends (Saturday and Sunday)
3. Optionally filter out holidays from a holiday calendar
4. Count the remaining dates

For adding business days, iterate forward, skipping weekends and holidays.
```

```java
// Count business days between two dates
public long countBusinessDays(LocalDate start, LocalDate end) {
    return start.datesUntil(end)
            .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY
                    && d.getDayOfWeek() != DayOfWeek.SUNDAY)
            .count();
}

// Add N business days to a date
public LocalDate addBusinessDays(LocalDate start, int days) {
    LocalDate result = start;
    int added = 0;
    while (added < days) {
        result = result.plusDays(1);
        if (result.getDayOfWeek() != DayOfWeek.SATURDAY
                && result.getDayOfWeek() != DayOfWeek.SUNDAY) {
            added++;
        }
    }
    return result;
}
```

**Q4: What are `TemporalAdjusters` and when should you use them?**

```text
A4: TemporalAdjusters is a utility class that provides predefined adjusters for 
common calendar operations. They implement the TemporalAdjuster interface and can 
be used with the with() method on any temporal type.

Common adjusters:
- firstDayOfMonth(), lastDayOfMonth()
- firstDayOfNextMonth(), firstDayOfYear()
- next(DayOfWeek), previous(DayOfWeek)
- nextOrSame(DayOfWeek), previousOrSame(DayOfWeek)
- dayOfWeekInMonth(ordinal, dayOfWeek) -- e.g., third Friday

You can also create custom adjusters for domain-specific logic like 
"next business day" or "next settlement date."
```

```java
LocalDate date = LocalDate.of(2024, 3, 15);

// Built-in adjusters
LocalDate lastDay = date.with(TemporalAdjusters.lastDayOfMonth());     // 2024-03-31
LocalDate nextMon = date.with(TemporalAdjusters.next(DayOfWeek.MONDAY)); // 2024-03-18
LocalDate thirdFri = date.with(
        TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.FRIDAY));       // 2024-03-15

// Custom adjuster: next business day
TemporalAdjuster nextBusinessDay = temporal -> {
    LocalDate d = LocalDate.from(temporal);
    do {
        d = d.plusDays(1);
    } while (d.getDayOfWeek() == DayOfWeek.SATURDAY
            || d.getDayOfWeek() == DayOfWeek.SUNDAY);
    return d;
};
LocalDate nextBiz = date.with(nextBusinessDay); // 2024-03-18 (Monday)
```

**Q5: How do you get the quarter of a year from a `LocalDate`?**

```text
A5: Java does not have a built-in getQuarter() method on LocalDate, but there 
are several ways to calculate it:

1. Simple arithmetic: (month - 1) / 3 + 1
2. Using IsoFields: date.get(IsoFields.QUARTER_OF_YEAR)
3. Using the Month enum: date.getMonth().firstMonthOfQuarter()

The IsoFields approach is the most "official" and handles edge cases correctly.
```

```java
LocalDate date = LocalDate.of(2024, 8, 15);

// Method 1: Arithmetic
int quarter1 = (date.getMonthValue() - 1) / 3 + 1; // 3

// Method 2: IsoFields (preferred)
int quarter2 = date.get(java.time.temporal.IsoFields.QUARTER_OF_YEAR); // 3

// Verification for all quarters
// Q1: Jan, Feb, Mar (months 1-3)
// Q2: Apr, May, Jun (months 4-6)
// Q3: Jul, Aug, Sep (months 7-9)
// Q4: Oct, Nov, Dec (months 10-12)
```

## Code Examples

- Test: [LocalDateTimeExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datetimeapi/LocalDateTimeExamplesTest.java)
- Source: [LocalDateTimeExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/datetimeapi/LocalDateTimeExamples.java)
