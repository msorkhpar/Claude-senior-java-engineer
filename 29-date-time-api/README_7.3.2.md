# 7.3.2. Overview of the java.time Package

A subscription service mails each customer on the day their 14-day trial ends. Support has two
tickets against it: customers in Sydney get the mail a day late, and a trial extended by a week
still ends on the old day. Nobody can reproduce the first in a test. Both come from one method:

```java
static boolean lastTrialDay(Account account) {
    LocalDate ends = account.trialStarted().toLocalDate().plusDays(14);   // trialStarted() is a LocalDateTime
    if (account.extended()) {
        ends.plusDays(7);
    }
    return LocalDate.now().equals(ends);
}
```

It compiles without a warning under `javac -Xlint:all`. Each section below fixes one of its
mistakes. Outputs are from Java 21.0.12 with tzdata 2026b, and each output names the
`-Duser.timezone` it ran with.

## Each Type Answers One Question, and Only Instant Is a Moment

`java.time` replaces `java.util.Date` and `Calendar`, where one class stood for a moment, a date
and a wall-clock reading (unit 7.3.1). Its main types each answer one question:

| Question | Type | Prints as | Details |
|---|---|---|---|
| When did it happen, for everyone? | `Instant` | `2026-03-31T22:30:00Z` | 7.3.2.2 |
| What did the clock on a wall in a place show? | `ZonedDateTime` | `2026-04-01T00:30+02:00[Europe/Amsterdam]` | 7.3.2.5 |
| Date and time with a fixed offset (no zone rules) | `OffsetDateTime` | `2026-04-01T00:30+02:00` | 7.3.2.5 |
| Date and time, place unknown | `LocalDateTime` | `2026-04-01T00:30` | 7.3.2.1 |
| Which calendar day? Which time of day? | `LocalDate`, `LocalTime` | `2026-04-01`, `00:30` | 7.3.2.1 |
| Which rules turn a moment into a wall-clock time? | `ZoneId`, `ZoneOffset` | `Europe/Amsterdam`, `+02:00` | 7.3.2.5 |
| How much time: exact, or in calendar units? | `Duration`, `Period` | `PT1H30M`, `P1M` | 7.3.2.3 |

The first six rows print one payment: an `Instant`, converted with `atZone`, then
`toOffsetDateTime()`, `toLocalDateTime()`, `toLocalDate()` and `toLocalTime()`, with
`getZone()` and `getOffset()` for the zone row. From the `ZonedDateTime` down, each conversion drops
something: the zone's rules, then the offset, then the date or the time. Going back up asks for
what was dropped:

```java
Instant paid = Instant.parse("2026-03-31T22:30:00Z");
ZonedDateTime zoned = paid.atZone(ZoneId.of("Europe/Amsterdam"));
LocalDateTime stored = zoned.toLocalDateTime();       // what a LocalDateTime column keeps
Instant back = Instant.from(stored);
```

```text
DateTimeException: Unable to obtain Instant from TemporalAccessor: 2026-04-01T00:30 of type java.time.LocalDateTime
```

A `LocalDateTime` is not a moment; turning it into one needs a zone or offset. A job that
supplies the wrong one gets a wrong moment and no error. The trial service's servers wrote
`trialStarted` in Amsterdam; a report job on a UTC server reads it back with
`stored.atZone(ZoneOffset.UTC).toInstant()`, next to the correct reading with Amsterdam's zone:

```text
read as UTC:       2026-04-01T00:30:00Z
read as Amsterdam: 2026-03-31T22:30:00Z
```

Every trial start read this way is two hours late, or one hour for a start written in winter,
because Amsterdam's offset changes with the season. `LocalDateTime` is for values with no place,
such as "the sale starts at 09:00 in every store".

## Between a Moment and a Date There Is Always a Zone

An `Instant` has no date. The date is a question about a place, so every conversion between the
two takes a `ZoneId`. Here is the payment's date in three places:

```java
Instant paid = Instant.parse("2026-03-31T22:30:00Z");
System.out.println(LocalDate.ofInstant(paid, ZoneId.of("America/New_York")));
System.out.println(LocalDate.ofInstant(paid, ZoneId.of("Europe/Amsterdam")));
System.out.println(LocalDate.ofInstant(paid, ZoneId.of("Australia/Sydney")));
```

```text
2026-03-31
2026-04-01
2026-04-01
```

A method without a zone argument still uses one, the JVM's default: `LocalDate.now()` reads the
system clock "in the default time-zone" (its javadoc). The same conversion with
`ZoneId.systemDefault()` on two servers, run with `-Duser.timezone=America/New_York` and then
`-Duser.timezone=Australia/Sydney`:

```text
LocalDate.ofInstant(paid, ZoneId.systemDefault()) = 2026-03-31
LocalDate.ofInstant(paid, ZoneId.systemDefault()) = 2026-04-01
```

The default zone is a deployment setting, not a fact about the customer. The mail job runs daily
at 22:30 on a UTC server. Take a Sydney customer whose trial ends on 1 April, even with `ends`
computed in Sydney's calendar: at 22:30 UTC on 31 March it is already 09:30 on 1 April in Sydney,
but the job's `LocalDate.now()` says 31 March. The mail goes out a day late.

<!-- practice 1 -->

## Values Never Change: Keep What plusDays Returns

### Spot the Problem: The Grace Period That Does Nothing

An invoice is due 30 days after issue, plus 7 for customers with a grace period:

```java
static LocalDate dueDate(LocalDate issued, boolean gracePeriod) {
    LocalDate due = issued.plusDays(30);
    if (gracePeriod) {
        due.plusDays(7);                       // (1)
    }
    return due;
}
```

For an invoice issued on 2026-03-02 (run with `-Duser.timezone=UTC`):

```text
no grace:   2026-04-01
with grace: 2026-04-01
```

Line (1) computes 2026-04-08 and throws it away. Every `java.time` value is immutable: `plusDays`,
`minusMonths`, `with(...)` and `withZoneSameInstant` return a new object and leave the one they
were called on unchanged. Code ported from `Calendar.add`, which changes the calendar in place,
looks correct and does nothing. The fix is `due = due.plusDays(7)`, and it prints
`2026-04-08` while `issued` stays `2026-03-02`.

The javadocs of `LocalDate`, `Instant`, `ZonedDateTime`, `Duration` and `Period` each say "This
class is immutable and thread-safe". Because of that, a `static final LocalDate` can be shared by every thread without a lock, and a getter can return its field
without the defensive copy a `Date` needs (unit 7.3.1).

## Code That Needs the Current Time Takes a Clock

`LocalDate.now()` hides two inputs, the current instant and the default zone, so a test can set
neither; `LocalDate.now(zone)` fixes only the zone. Its javadoc: "Using this method will prevent the ability to use an
alternate clock for testing because the clock is hard-coded." The fix is to take a `Clock` as a
constructor argument and ask it:

```java
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

public final class Trials {
    private final Clock clock;

    public Trials(Clock clock) {
        this.clock = clock;
    }

    /** True on the customer's last trial day, in the customer's own calendar. */
    public boolean lastDay(LocalDate trialEnds, ZoneId customerZone) {
        return LocalDate.now(clock.withZone(customerZone)).equals(trialEnds);
    }
}
```

`LocalDate.now(clock)` takes both inputs from the clock: its instant, dated in its zone. Production
passes `Clock.systemUTC()`. A test fixes the moment, and moves it with
`Clock.offset`:

```java
Clock at = Clock.fixed(Instant.parse("2026-03-31T22:30:00Z"), ZoneOffset.UTC);
LocalDate ends = LocalDate.of(2026, 4, 1);

Trials trials = new Trials(at);
System.out.println("Sydney:   " + trials.lastDay(ends, ZoneId.of("Australia/Sydney")));
System.out.println("New York: " + trials.lastDay(ends, ZoneId.of("America/New_York")));

Trials dayLater = new Trials(Clock.offset(at, Duration.ofDays(1)));
System.out.println("New York, a day later: " + dayLater.lastDay(ends, ZoneId.of("America/New_York")));
```

```text
Sydney:   true
New York: false
New York, a day later: true
```

The output is the same under `-Duser.timezone=UTC` and `-Duser.timezone=Asia/Kolkata`. A test
of the old `lastTrialDay` had to build its account relative to the current date, so it could only
test today on this machine, never the moment the Sydney bug needs.

Code that needs only the moment, never a date, can take an `InstantSource` (Java 17), which
`Clock` implements.

A test that needs time to move during a call writes a settable clock: `Clock` is an abstract class with three abstract methods, `instant()`, `getZone()` and
`withZone(ZoneId)`.

<!-- practice 2 -->

## Summary

- **Each type answers one question, and only `Instant` is a moment.** A `LocalDateTime` stored
  for "when it happened" turns back into a moment only with a zone someone has to guess.
- **Between a moment and a date there is always a zone.** A call without one uses the JVM's
  default, so the server's setting decides the customer's date.
- **Values never change.** `plusDays` and `with` return the answer; a dropped result is a silent
  no-op, and immutability is what makes the types safe to share.
- **Code that needs the current time takes a `Clock`.** Tests then choose the moment and the
  zone instead of depending on when and where they run.

## Reference

| Package | Holds |
|---|---|
| `java.time` | the value types above, `Clock`, `InstantSource` |
| `java.time.format` | `DateTimeFormatter` (7.3.2.4) |
| `java.time.temporal` | `ChronoUnit`, `ChronoField`, `TemporalAdjusters` |
| `java.time.zone` | `ZoneRules`, the time-zone rules a `ZoneId` refers to |
| `java.time.chrono` | non-ISO calendars, such as `JapaneseDate` |

| Clock | Gives |
|---|---|
| `Clock.systemUTC()` | the system clock, zone UTC |
| `Clock.systemDefaultZone()` | the system clock in the default zone, what the zone-dependent `now()` methods use |
| `Clock.fixed(instant, zone)` | always the same instant |
| `Clock.offset(clock, duration)` | another clock shifted by a duration |
| `clock.withZone(zone)` | the same instant source, another zone |

## Challenges

1. The trial service also stores `cancelledAt`. Which type would you choose, and what does each
   wrong choice cost when the company moves its servers from Amsterdam to Dublin?
2. A codebase has 400 calls to `LocalDate.now()`. Plan the move to an injected `Clock`: what do you
   change first, and how do you stop new calls from appearing?
3. A report must show "orders of 2026-03-29" for a shop in Amsterdam. Write the range of instants
   that query should select, and check whether it is 24 hours long.

## Design Note: Why the Zone Is an Argument

`java.time` could have kept `Date`'s approach and filled in the default zone wherever one was
missing. It does that only in the `now()` methods, `Clock.systemDefaultZone()` and
`ZoneId.systemDefault()`. Everywhere else the zone is a parameter, so the code says whose date it
is asking for, and a change to a server's default zone can't silently change those results. The
cost is longer calls and one more argument to pass through every layer that needs a date.

## Code Examples

This sub-topic provides an overview. See the following detailed sub-topic files for implementation and tests:
- [7.3.2.1. LocalDate, LocalTime, and LocalDateTime](README_7.3.2.1.md)
- [7.3.2.2. Instant and Timestamps](README_7.3.2.2.md)
- [7.3.2.3. Duration and Period](README_7.3.2.3.md)
- [7.3.2.4. DateTimeFormatter](README_7.3.2.4.md)
- [7.3.2.5. Zoned and Offset Date and Time](README_7.3.2.5.md)
