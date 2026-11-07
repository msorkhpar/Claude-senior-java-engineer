# 7.3.2.5. Zoned and Offset Date and Time

A team calendar stores its events as `OffsetDateTime`, the form its JSON API sends. The Amsterdam
office's weekly planning meeting was created for Thursday 26 March 2026 at 09:00. On 2 April every
phone in the office shows it at 10:00. Nobody edited it.

Outputs are from Java 21.0.12 with tzdata 2026b, run with `-Duser.timezone=UTC`; every zone in
the code is explicit, and `-Duser.timezone=Asia/Kolkata` printed the same lines.

## An OffsetDateTime Keeps One Offset; Only a ZonedDateTime Follows the Zone's Rules

An `Instant` is seconds and nanoseconds from 1970 (unit 7.3.2.2). An `OffsetDateTime` is a local
date-time plus an offset from UTC. A `ZonedDateTime` holds those two plus a `ZoneId`, whose rules
give the offset for any date:

```java
ZoneId amsterdam = ZoneId.of("Europe/Amsterdam");
ZonedDateTime zoned = ZonedDateTime.of(LocalDateTime.parse("2026-03-26T09:00"), amsterdam);
OffsetDateTime offset = zoned.toOffsetDateTime();
Instant instant = zoned.toInstant();
System.out.println(zoned + "   " + offset + "   " + instant);
System.out.println(zoned.plusWeeks(1));
System.out.println(offset.plusWeeks(1));
System.out.println(offset.plusWeeks(1).atZoneSameInstant(amsterdam));
```

```text
2026-03-26T09:00+01:00[Europe/Amsterdam]   2026-03-26T09:00+01:00   2026-03-26T08:00:00Z
2026-04-02T09:00+02:00[Europe/Amsterdam]
2026-04-02T09:00+01:00
2026-04-02T10:00+02:00[Europe/Amsterdam]
```

Amsterdam moved its clocks to `+02:00` on 29 March. The `ZonedDateTime` added a week to the wall
clock and asked the rules for the new offset. The `OffsetDateTime` has no rules to ask, so it kept
`+01:00`: its 09:00 is 08:00 UTC, which an Amsterdam phone shows as 10:00. (Adding a seven-day
`Duration` instead of a week is a different trap, unit 7.3.2.3.)

An `OffsetDateTime` suits a moment already fixed, such as a log line. Calendar arithmetic belongs
on a `ZonedDateTime`, converted to an offset for the wire at the end.

## A Zone Written as an Offset Has No Rules

A `ZoneOffset` is a `ZoneId`, and text without a region name produces one:

```java
ZonedDateTime fromApi = ZonedDateTime.parse("2026-03-26T09:00+01:00");
System.out.println(fromApi.getZone() + " -> " + fromApi.plusWeeks(1));
ZonedDateTime withRegion = ZonedDateTime.parse("2026-03-26T09:00+01:00[Europe/Amsterdam]");
System.out.println(withRegion.getZone() + " -> " + withRegion.plusWeeks(1));
```

```text
+01:00 -> 2026-04-02T09:00+01:00
Europe/Amsterdam -> 2026-04-02T09:00+02:00[Europe/Amsterdam]
```

The first value's zone is a `ZoneOffset`, as `ZoneId.of("+01:00")` would give, so its rules are
"`+01:00` forever" and it behaves like the `OffsetDateTime` above. Nor can an offset name the region: in March
`+01:00` is Amsterdam, Lagos and Algiers, and only Amsterdam changes on 29 March. An API
carrying future times sends the region id too. Abbreviations are no substitute:
`ZoneId.of("CST")` throws `ZoneRulesException`, while `"CET"` parses as a legacy id.

## withZoneSameInstant Converts a Moment; withZoneSameLocal Moves It

A call is set for 15:00 on 20 March in Amsterdam, and a colleague in New York opens the invite:

```java
ZonedDateTime call = ZonedDateTime.of(LocalDateTime.parse("2026-03-20T15:00"), amsterdam);
ZoneId newYork = ZoneId.of("America/New_York");
System.out.println(call.withZoneSameInstant(newYork));
System.out.println(call.withZoneSameLocal(newYork));
```

```text
2026-03-20T10:00-04:00[America/New_York]
2026-03-20T15:00-04:00[America/New_York]
```

`withZoneSameInstant` keeps the moment: 10:00, five hours behind
rather than the usual six, because New York started summer time on 8 March and Amsterdam waits
until 29 March. A hard-coded "minus six hours" is wrong for three weeks in spring and one in autumn.
`withZoneSameLocal` keeps the wall clock, which names a moment five hours later: a colleague shown
that invite joins after the call has ended. Use it only when the meeting itself moves.

<!-- practice 1 -->

## Near a Clock Change a Local Time Can Be Missing or Doubled, and ZonedDateTime Picks One Without Telling You

On 29 March 2026 Amsterdam's clocks jump from 02:00 to 03:00, so 02:30 never happens. On 25 October
they go from 03:00 back to 02:00, so 02:30 happens twice, first at `+02:00` and an hour later at
`+01:00`. A maintenance window set for 02:30 on both Sundays:

```java
ZonedDateTime gap = ZonedDateTime.of(LocalDateTime.parse("2026-03-29T02:30"), amsterdam);
ZonedDateTime twice = ZonedDateTime.of(LocalDateTime.parse("2026-10-25T02:30"), amsterdam);
System.out.println(gap);
System.out.println(twice);
System.out.println(twice.withLaterOffsetAtOverlap());
```

```text
2026-03-29T03:30+02:00[Europe/Amsterdam]
2026-10-25T02:30+02:00[Europe/Amsterdam]
2026-10-25T02:30+01:00[Europe/Amsterdam]
```

In a gap, `ZonedDateTime` moves the time later by the gap's length, to 03:30 rather than 03:00.
In an overlap `of` takes the earlier offset, while arithmetic keeps the current one if it stays
valid; `withLaterOffsetAtOverlap()` picks the second 02:30. Whether that is right depends on the job: a window an operator confirmed
for 02:30 should not quietly start at 03:30, and a backup that must follow the night's last write
wants the second 02:30.

To decide yourself, ask the rules first: `getValidOffsets` returns no offset in a gap, two in an
overlap and one otherwise:

```java
ZoneRules rules = amsterdam.getRules();
System.out.println(rules.getValidOffsets(LocalDateTime.parse("2026-03-29T02:30")) + " "
        + rules.getValidOffsets(LocalDateTime.parse("2026-10-25T02:30")) + " "
        + rules.getValidOffsets(LocalDateTime.parse("2026-10-25T03:30")));
```

```text
[] [+02:00, +01:00] [+01:00]
```

### Spot the Problem: The Weekly Maintenance Window

The weekly window starts on 22 March at 02:30; the scheduler computes each start from the
previous one:

```java
ZonedDateTime first = ZonedDateTime.of(LocalDateTime.parse("2026-03-22T02:30"), amsterdam);
ZonedDateTime stepped = first;
for (int week = 1; week <= 2; week++) {
    stepped = stepped.plusWeeks(1);
    System.out.println("week " + week + ": stepped " + stepped.toLocalDateTime()
            + ", from first " + first.plusWeeks(week).toLocalDateTime());
}
```

```text
week 1: stepped 2026-03-29T03:30, from first 2026-03-29T03:30
week 2: stepped 2026-04-05T03:30, from first 2026-04-05T02:30
```

The gap moved week 1 to 03:30, and every later start inherits it. Computing occurrence `k` from
the first one, `first.plusWeeks(k)`, confines the shift to the week the gap falls in.

<!-- practice 2 -->

<!-- practice 3 -->

## Store a Future Appointment as Local Time and Zone, and Work Out the Instant Late

A clinic books a check-up for 1 July 2027 at 09:00 in Amsterdam. Saving it as an `Instant` fixes
today's offset, `+02:00`. Rules change by law, and a JDK learns of it through a tzdata update.
Suppose the Netherlands abolished summer time before then, modelled as a fixed `+01:00`:

```java
LocalDateTime booked = LocalDateTime.parse("2027-07-01T09:00");
Instant saved = booked.atZone(amsterdam).toInstant();       // computed with today's rules
ZoneRules later = ZoneRules.of(ZoneOffset.ofHours(1));      // suppose summer time is abolished
System.out.println("saved instant " + saved + " -> " + saved.atOffset(later.getOffset(saved)));
OffsetDateTime resolved = booked.atOffset(later.getOffset(booked));
System.out.println("local + zone  " + resolved.toInstant() + " -> " + resolved);
```

```text
saved instant 2027-07-01T07:00:00Z -> 2027-07-01T08:00+01:00
local + zone  2027-07-01T08:00:00Z -> 2027-07-01T09:00+01:00
```

The saved instant now reads 08:00: the reminder and the doctor's day view both put the
appointment an hour early. Patient and doctor agreed on "09:00 in Amsterdam", and the instant
follows from that and the rules in force on the day. Store a future appointment as a
`LocalDateTime` and a region id, and convert with `atZone` when a reminder is due. A past event,
such as a payment, is an `Instant`.

<!-- practice 4 -->

## Summary

- **An `OffsetDateTime` keeps one offset; a `ZonedDateTime` follows the zone's rules.** Calendar
  arithmetic on an offset is off once it crosses a clock change.
- **A zone written as an offset has no rules.** Carry the region id.
- **`withZoneSameInstant` converts; `withZoneSameLocal` moves the moment.** The difference between
  two zones changes during the year.
- **A local time near a clock change may be missing or doubled.** `ZonedDateTime` resolves both
  silently; check `getValidOffsets` when it matters, and step from the first occurrence.
- **A future appointment is a local time and a zone id.** An instant saved today is wrong after
  the rules change; past events are instants.

## Reference

| Call | Result (Java 21, tzdata 2026b) |
|---|---|
| `withEarlierOffsetAtOverlap()`, `withLaterOffsetAtOverlap()` | first or second occurrence; the same object outside an overlap |
| `ZonedDateTime.ofLocal(local, zone, preferredOffset)` | in an overlap, the preferred offset if it is one of the two |
| `ZonedDateTime.ofStrict(local, offset, zone)` | `DateTimeException` if the local time and offset don't exist in the zone |
| `a.equals(b)` on `ZonedDateTime` or `OffsetDateTime` | compares local date-time and offset, and the zone of a `ZonedDateTime`: the same moment in another zone or offset is `false` |
| `a.isEqual(b)` | compares the instant only |
| `ZoneId.of("+01:00")` | a `ZoneOffset`: one fixed offset |
| `ZoneId.of("Z")` | `ZoneOffset.UTC` itself |
| `ZoneId.of("UTC")` | a region id, not `equals` to `ZoneOffset.UTC` |
| `ZoneRulesProvider.getVersions(id).lastKey()` | the tzdata version in use |

## Challenges

1. A booking form sends `2026-10-25T02:30` and `Europe/Amsterdam`. Which occurrence do you book,
   and how does the user choose?
2. A report lists past payments (instants) and future bookings (local time plus zone id) in time
   order. Where do you convert, and with which rules?
3. A JDK update changes the rules of a zone your customers live in. Which stored values change
   meaning, and how do you find the bookings affected?

## Design Note: Why ZonedDateTime Resolves Instead of Throwing

`ZonedDateTime.of` returns a value for every valid date and time. Arithmetic such as `plusDays`
near a transition therefore always has a result, and code tested in summer doesn't start throwing
on the nights the clocks change. The cost is that the choice is silent: a scheduler that has to know asks
`getValidOffsets` or uses `ofStrict`, and the rest of the code runs on a policy it may not know
exists.

## Code Examples

- Test: [ZonedDateTimeExamplesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datetimeapi/ZonedDateTimeExamplesTest.java)
- Source: [ZonedDateTimeExamples.java](src/main/java/com/github/msorkhpar/claudejavatutor/datetimeapi/ZonedDateTimeExamples.java)
