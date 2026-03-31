# 7.3.2.4. DateTimeFormatter

## Concept Explanation

`DateTimeFormatter` is the modern replacement for `SimpleDateFormat`, providing a thread-safe, immutable way to format and parse date/time objects in the `java.time` package. It supports predefined ISO-8601 formats, localized styles, and custom patterns.

**Real-world analogy**: Think of a `DateTimeFormatter` as a stencil for writing dates. Once you cut the stencil (create the formatter), it always produces the same format. Unlike `SimpleDateFormat` (a shared pen that multiple people fight over), the stencil can be used by any number of people simultaneously without interference because it never changes.

`DateTimeFormatter` is in the `java.time.format` package and works with all temporal types: `LocalDate`, `LocalTime`, `LocalDateTime`, `ZonedDateTime`, `OffsetDateTime`, and `Instant`.

## Key Points to Remember

1. **Immutable and thread-safe** -- a single static instance can be shared across all threads.
2. **Predefined formatters**: `ISO_LOCAL_DATE`, `ISO_LOCAL_DATE_TIME`, `ISO_ZONED_DATE_TIME`, etc.
3. **Pattern letters**: `y` (year), `M` (month), `d` (day), `H` (hour 0-23), `h` (hour 1-12), `m` (minute), `s` (second), `S` (fraction), `E` (day of week), `a` (AM/PM), `z` (timezone name), `Z` (offset).
4. **Locale-aware**: `ofLocalizedDate()`, `ofLocalizedTime()`, `ofLocalizedDateTime()` produce locale-specific output.
5. **`withLocale()`** and **`withZone()`** return new formatter instances (immutable).
6. **Parsing is strict by default** -- invalid dates throw `DateTimeParseException`.
7. **Use `ofPattern()`** for custom patterns; combine with `withLocale()` for localized patterns.

## Relevant Java 21 Features

- `DateTimeFormatter` has been stable since Java 8 with minor additions through Java 21.
- The `'B'` pattern letter (period of day, e.g., "in the morning") was added in Java 16.
- Virtual threads benefit from thread-safety -- no `ThreadLocal` workaround needed.
- Integration with records for clean DTO formatting patterns.

## Common Pitfalls and How to Avoid Them

1. **Using `yyyy` when you mean `uuuu`**:
   ```java
   // Problem: 'y' is year-of-era (always positive), 'u' is proleptic year (supports negative)
   // For dates before year 1, 'yyyy' produces unexpected results
   DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Year 0 = "0001" (era-based)

   // Fix: Use 'uuuu' for proleptic year if negative years are possible
   DateTimeFormatter.ofPattern("uuuu-MM-dd"); // Year 0 = "0000"
   ```

2. **Wrong case for pattern letters**:
   ```java
   // Problem: 'mm' is minutes, 'MM' is months
   DateTimeFormatter.ofPattern("yyyy-mm-dd"); // WRONG: minutes instead of months!

   // Fix: Use correct case
   DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Correct
   ```

3. **Formatting `Instant` without a zone**:
   ```java
   // Problem: Instant has no timezone, formatter needs one
   Instant instant = Instant.now();
   DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(instant);
   // UnsupportedTemporalTypeException!

   // Fix: Provide a zone
   DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
           .withZone(ZoneId.of("UTC"))
           .format(instant);
   ```

4. **Parsing a date string into the wrong type**:
   ```java
   // Problem: Trying to parse a date-only string into LocalDateTime
   LocalDateTime.parse("2024-03-15", DateTimeFormatter.ISO_LOCAL_DATE);
   // DateTimeParseException!

   // Fix: Use the matching type
   LocalDate.parse("2024-03-15", DateTimeFormatter.ISO_LOCAL_DATE);
   ```

## Best Practices and Optimization Techniques

1. **Declare formatters as `static final` constants** -- they are immutable and thread-safe.
2. **Use predefined ISO formatters** when possible instead of custom patterns.
3. **Always specify a `Locale`** for formatters that produce locale-sensitive output (month names, day names).
4. **Use `DateTimeFormatter` for all formatting/parsing** -- never use `SimpleDateFormat` in new code.
5. **Handle `DateTimeParseException` explicitly** when parsing user input.

## Edge Cases and Their Handling

1. **Padding and optional sections**: Use `[]` for optional parts: `ofPattern("yyyy-MM-dd['T'HH:mm:ss]")`.
2. **Single-digit months/days**: `M` produces 1-digit (3), `MM` produces 2-digit (03).
3. **12-hour vs 24-hour format**: `H` is 0-23 (24h), `h` is 1-12 (12h, needs `a` for AM/PM).
4. **Locale-dependent month names**: `MMMM` produces full month name in the formatter's locale.
5. **Empty or null strings**: `parse()` throws `DateTimeParseException` for empty strings and `NullPointerException` for null.

## Interview-specific Insights

Interviewers test:
- Thread-safety awareness (contrast with `SimpleDateFormat`)
- Knowledge of pattern letters and their correct usage
- Ability to create custom formatters for specific formats
- Understanding of locale-sensitive formatting
- Error handling during parsing

## Interview Q&A Section

**Q1: Why is `DateTimeFormatter` thread-safe while `SimpleDateFormat` is not?**

```text
A1: DateTimeFormatter is thread-safe because it is immutable. Once created, its 
internal state never changes. All methods that appear to modify it (withLocale, 
withZone) actually return new instances.

SimpleDateFormat is NOT thread-safe because it uses mutable internal state -- 
specifically, a Calendar instance that is modified during format() and parse() 
operations. When multiple threads call these methods concurrently, they corrupt 
the shared Calendar, producing incorrect results or exceptions.

Pre-Java 8 workarounds for SimpleDateFormat:
1. Synchronize access (kills concurrency)
2. Create new instances per use (garbage creation)
3. ThreadLocal<SimpleDateFormat> (memory leaks, complex lifecycle)

DateTimeFormatter eliminates all of these problems by design.
```

```java
// SimpleDateFormat: UNSAFE for sharing
private static final SimpleDateFormat UNSAFE = new SimpleDateFormat("yyyy-MM-dd");
// Concurrent calls to UNSAFE.format() corrupt results

// DateTimeFormatter: SAFE for sharing
private static final DateTimeFormatter SAFE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
// Any number of threads can call SAFE.format() simultaneously
```

**Q2: What are the common pattern letters and how do they differ?**

```text
A2: Key pattern letters and their common confusions:

Date:
- y/u: year (y = year-of-era, u = proleptic year)
- M: month (M = number, MM = 2-digit, MMM = abbreviated, MMMM = full)
- d: day of month (d = 1-digit, dd = 2-digit)
- E: day of week (E = abbreviated, EEEE = full)

Time:
- H: hour 0-23 (24-hour clock)
- h: hour 1-12 (12-hour clock, pair with 'a')
- m: minute of hour
- s: second of minute
- S: fraction of second (S = 1/10, SS = 1/100, SSS = millisecond)
- a: AM/PM marker

Zone:
- z: timezone name (e.g., "EST", "Eastern Standard Time")
- Z: timezone offset (e.g., "+0500")
- X: ISO offset (e.g., "+05", "+05:30", "Z" for UTC)
- VV: timezone ID (e.g., "America/New_York")

Common confusion: 
- 'M' (month) vs 'm' (minute) -- case matters!
- 'H' (24-hour) vs 'h' (12-hour)
- 'y' (year-of-era) vs 'u' (proleptic year)
```

```java
LocalDateTime dt = LocalDateTime.of(2024, 3, 15, 14, 30, 45);

// Date patterns
DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dt);         // "2024-03-15"
DateTimeFormatter.ofPattern("dd/MM/yyyy").format(dt);         // "15/03/2024"
DateTimeFormatter.ofPattern("MMMM d, yyyy").format(dt);       // "March 15, 2024"
DateTimeFormatter.ofPattern("EEE, MMM d").format(dt);         // "Fri, Mar 15"

// Time patterns
DateTimeFormatter.ofPattern("HH:mm:ss").format(dt);           // "14:30:45"
DateTimeFormatter.ofPattern("hh:mm a").format(dt);            // "02:30 PM"

// Combined
DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(dt); // "2024-03-15 14:30:45"
```

**Q3: How do you handle parsing errors gracefully?**

```text
A3: DateTimeFormatter.parse() and LocalDate.parse() throw DateTimeParseException 
for invalid input. For user-facing applications, you should catch this exception 
and provide meaningful feedback.

Strategies:
1. Try-catch with specific error handling
2. Create a safe parse method that returns Optional
3. Use multiple formatters for flexible input acceptance
4. Validate format before parsing with regex (for simple cases)
```

```java
// Strategy 1: Try-catch
public LocalDate parseUserInput(String input) {
    try {
        return LocalDate.parse(input, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid date format. Expected MM/dd/yyyy", e);
    }
}

// Strategy 2: Safe parse returning Optional
public Optional<LocalDate> safeParse(String input, String pattern) {
    try {
        return Optional.of(LocalDate.parse(input, DateTimeFormatter.ofPattern(pattern)));
    } catch (DateTimeParseException e) {
        return Optional.empty();
    }
}

// Strategy 3: Multiple format support
public LocalDate flexibleParse(String input) {
    List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    );
    for (DateTimeFormatter fmt : formatters) {
        try { return LocalDate.parse(input, fmt); }
        catch (DateTimeParseException ignored) {}
    }
    throw new IllegalArgumentException("Cannot parse date: " + input);
}
```

**Q4: How do you format an `Instant` for display?**

```text
A4: Instant cannot be directly formatted with most patterns because it has no 
calendar or timezone fields. You must either:

1. Use withZone() on the formatter to provide timezone context
2. Convert Instant to ZonedDateTime first and then format

Option 1 is more concise; option 2 gives you more control.
```

```java
Instant instant = Instant.parse("2024-03-15T14:30:00Z");

// Option 1: Formatter with zone
DateTimeFormatter formatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss z")
        .withZone(ZoneId.of("America/New_York"));
String result = formatter.format(instant);
// "2024-03-15 10:30:00 EDT"

// Option 2: Convert to ZonedDateTime first
ZonedDateTime zdt = instant.atZone(ZoneId.of("America/New_York"));
String result2 = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
// "2024-03-15 10:30:00 EDT"
```

**Q5: How does locale affect date formatting?**

```text
A5: Locale affects:
1. Month names (March vs. Mars vs. Marzo)
2. Day-of-week names (Monday vs. Lundi)
3. AM/PM markers
4. Number formatting in some locales
5. Date order in localized styles (MEDIUM, FULL, etc.)

You specify locale in two ways:
- ofPattern(pattern, locale): Creates a formatter with a specific locale
- withLocale(locale): Returns a new formatter with the specified locale

Always specify locale explicitly for user-facing output to ensure consistent 
behavior regardless of the server's default locale.
```

```java
LocalDate date = LocalDate.of(2024, 3, 15);

// Locale-specific month names
DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy");
monthFmt.withLocale(Locale.US).format(date);      // "March 2024"
monthFmt.withLocale(Locale.FRANCE).format(date);   // "mars 2024"
monthFmt.withLocale(Locale.GERMANY).format(date);  // "M\u00E4rz 2024"
monthFmt.withLocale(Locale.JAPAN).format(date);     // "3\u6708 2024"

// Localized date styles
DateTimeFormatter localizedFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
localizedFmt.withLocale(Locale.US).format(date);     // "Friday, March 15, 2024"
localizedFmt.withLocale(Locale.FRANCE).format(date);  // "vendredi 15 mars 2024"
```

## Code Examples

- Test: [DateTimeFormatterExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datetimeapi/DateTimeFormatterExamplesTest.java)
- Source: [DateTimeFormatterExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/datetimeapi/DateTimeFormatterExamples.java)
