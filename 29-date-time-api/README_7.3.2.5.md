# 7.3.2.5. Zoned and Offset Date and Time

## Concept Explanation

`ZonedDateTime` and `OffsetDateTime` are the timezone-aware temporal types in `java.time`. They extend `LocalDateTime` by adding timezone context, making them suitable for scenarios where the exact point on the global timeline matters.

**Real-world analogy**: Imagine a global conference call. If you say "let's meet at 2:00 PM" (`LocalDateTime`), nobody knows which 2:00 PM you mean. If you say "2:00 PM Eastern Time" (`ZonedDateTime`), everyone can convert to their local time. If you say "2:00 PM UTC-5" (`OffsetDateTime`), the meaning is unambiguous but you lose the semantic connection to "Eastern Time" (which might be -4 during daylight saving).

- **`ZonedDateTime`**: Date + time + timezone rules (e.g., `America/New_York`). Knows about DST transitions and automatically adjusts.
- **`OffsetDateTime`**: Date + time + fixed UTC offset (e.g., `+05:30`). Simpler, no DST awareness.
- **`ZoneId`**: A timezone identifier (e.g., `America/New_York`, `Europe/London`). Contains the rules for DST and historical offset changes.
- **`ZoneOffset`**: A fixed offset from UTC (e.g., `+05:30`, `-04:00`, `Z`).

## Key Points to Remember

1. **`ZonedDateTime` handles DST automatically** -- gap and overlap adjustments are built in.
2. **`OffsetDateTime` is simpler** -- no DST rules, just a fixed offset.
3. **`ZoneId.of("UTC")` vs `ZoneOffset.UTC`** -- both represent UTC, but `ZoneId` is a zone (with rules), `ZoneOffset` is a fixed offset.
4. **`withZoneSameInstant()`** converts timezone, preserving the instant (different local time).
5. **`withZoneSameLocal()`** changes timezone, preserving local time (different instant).
6. **600+ timezone IDs** are available via `ZoneId.getAvailableZoneIds()`.
7. **Prefer `ZonedDateTime` for user-facing times** and `OffsetDateTime` for API/database storage.
8. **`equals()` checks zone too** -- use `isEqual()` or compare `toInstant()` for same-instant checks.

## Relevant Java 21 Features

- ZonedDateTime and OffsetDateTime have been stable since Java 8.
- Timezone database (IANA/Olson) is updated with each JDK release to reflect geopolitical timezone changes.
- Virtual threads commonly use `ZonedDateTime` for scheduling tasks across timezones.

## Common Pitfalls and How to Avoid Them

1. **Using `equals()` to compare across timezones**:
   ```java
   // Problem: equals() checks zone, not just the instant
   ZonedDateTime ny = ZonedDateTime.parse("2024-03-15T12:00-04:00[America/New_York]");
   ZonedDateTime utc = ny.withZoneSameInstant(ZoneId.of("UTC"));
   ny.equals(utc); // false!

   // Fix: Use isEqual() or compare instants
   ny.isEqual(utc); // true -- same moment in time
   ny.toInstant().equals(utc.toInstant()); // true
   ```

2. **Ignoring DST gaps**:
   ```java
   // Problem: Creating a time that doesn't exist during spring forward
   ZoneId ny = ZoneId.of("America/New_York");
   // On March 10, 2024, 2:00 AM doesn't exist (clocks skip to 3:00 AM)
   ZonedDateTime gap = ZonedDateTime.of(2024, 3, 10, 2, 30, 0, 0, ny);
   // Silently adjusted to 3:30 AM!

   // Awareness: Always check if the resulting time matches your input
   ```

3. **Confusing `OffsetDateTime` with `ZonedDateTime`**:
   ```java
   // Problem: OffsetDateTime doesn't track DST
   OffsetDateTime odt = OffsetDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneOffset.ofHours(-5));
   // In summer, Eastern Time is -4, not -5. OffsetDateTime doesn't know this.

   // Fix: Use ZonedDateTime when DST matters
   ZonedDateTime zdt = ZonedDateTime.of(2024, 7, 15, 12, 0, 0, 0, ZoneId.of("America/New_York"));
   // Automatically uses -4 (EDT) in summer
   ```

4. **Using three-letter timezone abbreviations**:
   ```java
   // Problem: "CST" is ambiguous (Central Standard Time? China Standard Time?)
   ZoneId.of("CST"); // May throw or resolve unexpectedly

   // Fix: Use IANA timezone IDs
   ZoneId.of("America/Chicago");  // Central US
   ZoneId.of("Asia/Shanghai");    // China
   ```

## Best Practices and Optimization Techniques

1. **Use IANA timezone IDs** (e.g., `America/New_York`) instead of abbreviations.
2. **Store timestamps as `Instant`** in databases; convert to `ZonedDateTime` for display.
3. **Use `OffsetDateTime`** for API responses and database columns (simpler, no DST ambiguity).
4. **Use `ZonedDateTime`** for scheduling and user-facing features where DST matters.
5. **Cache `ZoneId.of()` results** in constants for frequently used timezones.

## Edge Cases and Their Handling

1. **DST gap (spring forward)**: `ZonedDateTime` adjusts the time forward past the gap.
2. **DST overlap (fall back)**: `ZonedDateTime` uses the earlier offset by default; use `withEarlierOffsetAtOverlap()`/`withLaterOffsetAtOverlap()` to choose.
3. **Historical timezone changes**: `ZoneId` respects historical rules (e.g., timezone changes in past decades).
4. **Comparing dates across the date line**: Two ZonedDateTimes can show different calendar dates but represent the same instant.
5. **`ZoneId.of("Z")`** returns `ZoneOffset.UTC`, not a ZoneId with "Z" as its name.

## Interview-specific Insights

Interviewers probe:
- Understanding of DST and its impact on time calculations
- Difference between `ZonedDateTime` and `OffsetDateTime`
- `withZoneSameInstant()` vs `withZoneSameLocal()`
- How to properly compare times across timezones
- When to use each timezone-aware type

## Interview Q&A Section

**Q1: What is the difference between `ZonedDateTime` and `OffsetDateTime`?**

```text
A1: The key difference is DST awareness:

ZonedDateTime:
- Carries a ZoneId (e.g., "America/New_York")
- Knows about DST rules -- automatically adjusts offset as DST changes
- Same zone ID can have different offsets at different times of year
- Best for: scheduling, user-facing times, calendar applications

OffsetDateTime:
- Carries a fixed ZoneOffset (e.g., "-04:00")
- No DST awareness -- the offset never changes
- Simpler and more predictable
- Best for: API responses, database storage, log timestamps

Conversion: ZonedDateTime -> OffsetDateTime is lossless (via toOffsetDateTime()).
OffsetDateTime -> ZonedDateTime requires specifying which zone the offset belongs to.

SQL/JDBC: OffsetDateTime maps directly to TIMESTAMP WITH TIME ZONE.
```

```java
// ZonedDateTime: DST-aware
ZoneId ny = ZoneId.of("America/New_York");
ZonedDateTime winter = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, ny);
ZonedDateTime summer = ZonedDateTime.of(2024, 7, 15, 12, 0, 0, 0, ny);
System.out.println(winter.getOffset()); // -05:00 (EST)
System.out.println(summer.getOffset()); // -04:00 (EDT)

// OffsetDateTime: Fixed offset
OffsetDateTime odt = OffsetDateTime.of(2024, 3, 15, 12, 0, 0, 0, ZoneOffset.ofHours(-5));
// Always -05:00, regardless of DST
```

**Q2: How does java.time handle DST gaps and overlaps?**

```text
A2: During DST transitions, two special cases occur:

1. Gap (spring forward): A range of local times does not exist. For example, on 
   March 10, 2024 in US Eastern, 2:00 AM to 2:59 AM is skipped.
   - ZonedDateTime.of() for a time in the gap adjusts FORWARD to the post-gap time
   - 2:30 AM becomes 3:30 AM EDT

2. Overlap (fall back): A range of local times occurs twice. For example, on 
   November 3, 2024 in US Eastern, 1:00 AM to 1:59 AM occurs twice (EDT then EST).
   - ZonedDateTime.of() for a time in the overlap uses the EARLIER offset (pre-transition)
   - Use withLaterOffsetAtOverlap() to select the later occurrence
   - Use withEarlierOffsetAtOverlap() to explicitly select the earlier occurrence

The API never throws an exception for DST transitions -- it makes a reasonable 
default choice and provides methods to override it.
```

```java
ZoneId ny = ZoneId.of("America/New_York");

// Gap: March 10, 2024, 2:00 AM doesn't exist
ZonedDateTime gap = ZonedDateTime.of(2024, 3, 10, 2, 30, 0, 0, ny);
System.out.println(gap); // 2024-03-10T03:30-04:00[America/New_York]
// Adjusted forward to 3:30 AM EDT

// Overlap: November 3, 2024, 1:00 AM occurs twice
ZonedDateTime overlap = ZonedDateTime.of(2024, 11, 3, 1, 30, 0, 0, ny);
System.out.println(overlap); // 2024-11-03T01:30-04:00 (EDT -- earlier offset)

ZonedDateTime later = overlap.withLaterOffsetAtOverlap();
System.out.println(later); // 2024-11-03T01:30-05:00 (EST -- later offset)
```

**Q3: What is the difference between `withZoneSameInstant()` and `withZoneSameLocal()`?**

```text
A3: These two methods change the timezone of a ZonedDateTime in fundamentally 
different ways:

withZoneSameInstant(zone):
- Preserves the INSTANT (same point in time)
- Changes the local date/time to match the new zone
- Example: 2 PM in New York = 7 PM in London (same moment)
- Use case: "What time is it in London right now?"

withZoneSameLocal(zone):
- Preserves the LOCAL DATE/TIME (same wall clock reading)
- Changes the instant (different point in time)
- Example: 2 PM in New York becomes "2 PM in London" (different moment)
- Use case: "Schedule this meeting at 2 PM London time instead"

This is one of the most important distinctions in timezone programming.
```

```java
ZonedDateTime nyNoon = ZonedDateTime.of(2024, 3, 15, 12, 0, 0, 0,
        ZoneId.of("America/New_York"));

// withZoneSameInstant: SAME moment, different local time
ZonedDateTime londonSameInstant = nyNoon.withZoneSameInstant(ZoneId.of("Europe/London"));
// 2024-03-15T16:00+00:00[Europe/London] -- 4 PM London = 12 PM New York
// Same instant: nyNoon.toInstant().equals(londonSameInstant.toInstant()) == true

// withZoneSameLocal: SAME local time, different moment
ZonedDateTime londonSameLocal = nyNoon.withZoneSameLocal(ZoneId.of("Europe/London"));
// 2024-03-15T12:00+00:00[Europe/London] -- 12 PM London (different moment!)
// Different instant: nyNoon.toInstant().equals(londonSameLocal.toInstant()) == false
```

**Q4: How do you find the current time in multiple timezones?**

```text
A4: Get the current Instant and convert to each desired timezone:

1. Instant.now() gives you the current moment
2. instant.atZone(ZoneId) converts to any timezone
3. ZonedDateTime.now(ZoneId) is a shortcut for the above

This pattern is correct because it ensures all conversions are based on the 
same instant, avoiding subtle timing issues.
```

```java
// Get current time in multiple zones
Instant now = Instant.now();

ZonedDateTime newYork = now.atZone(ZoneId.of("America/New_York"));
ZonedDateTime london = now.atZone(ZoneId.of("Europe/London"));
ZonedDateTime tokyo = now.atZone(ZoneId.of("Asia/Tokyo"));
ZonedDateTime sydney = now.atZone(ZoneId.of("Australia/Sydney"));

// Or use the shortcut
ZonedDateTime nyDirect = ZonedDateTime.now(ZoneId.of("America/New_York"));

// Format for display
DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("HH:mm z");
System.out.println("New York: " + newYork.format(displayFmt));
System.out.println("London:   " + london.format(displayFmt));
System.out.println("Tokyo:    " + tokyo.format(displayFmt));
System.out.println("Sydney:   " + sydney.format(displayFmt));
```

**Q5: When should you use `ZoneId` vs `ZoneOffset`?**

```text
A5: Use ZoneId when:
- You need DST awareness (most user-facing scenarios)
- You're working with named timezones ("America/New_York")
- You're scheduling future events (DST rules may change offset)
- You need historical accuracy (past timezone changes)

Use ZoneOffset when:
- You have a fixed offset from UTC
- You're storing/transmitting timestamps (API, database)
- DST is not relevant (e.g., UTC-based timestamps)
- You need maximum simplicity and predictability

ZoneOffset is a subset of ZoneId (it implements ZoneId). Every ZoneOffset is 
also a valid ZoneId, but not every ZoneId is a ZoneOffset.

Special note: ZoneId.of("UTC"), ZoneId.of("Z"), and ZoneOffset.UTC all 
represent UTC but are different types. Use ZoneOffset.UTC when you specifically 
need an offset value.
```

```java
// ZoneId: DST-aware, named timezone
ZoneId newYork = ZoneId.of("America/New_York"); // DST rules included
ZonedDateTime zdt = ZonedDateTime.now(newYork);
// Offset changes between -05:00 (winter) and -04:00 (summer)

// ZoneOffset: Fixed, simple offset
ZoneOffset utcPlus5 = ZoneOffset.ofHours(5);
OffsetDateTime odt = OffsetDateTime.now(utcPlus5);
// Always +05:00, no DST changes

// UTC representations
ZoneOffset utc1 = ZoneOffset.UTC;              // ZoneOffset
ZoneId utc2 = ZoneId.of("UTC");                // ZoneId wrapping ZoneOffset
ZoneId utc3 = ZoneId.of("Z");                  // Same as UTC
```

## Code Examples

- Test: [ZonedDateTimeExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datetimeapi/ZonedDateTimeExamplesTest.java)
- Source: [ZonedDateTimeExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/datetimeapi/ZonedDateTimeExamples.java)
