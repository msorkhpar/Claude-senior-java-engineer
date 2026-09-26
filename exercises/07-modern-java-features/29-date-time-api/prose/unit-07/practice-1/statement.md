A calendar app stores each meeting as a `ZonedDateTime` in its organiser's zone.
Write two methods on `Meetings`:

- `forAttendee(ZonedDateTime meeting, ZoneId attendee)`: the meeting as an
  attendee in `attendee` sees it. It is the **same moment**, read on the
  attendee's own clock, and that can be another calendar day.
- `reschedule(ZonedDateTime meeting, ZoneId newZone)`: the organiser moves the
  meeting to another office and keeps its **wall-clock time**: noon stays noon,
  now in `newZone`. That is a different moment.

| meeting | zone | forAttendee | reschedule |
|---|---|---|---|
| `2024-03-15T12:00` `America/New_York` | `America/Toronto` | `2024-03-15T12:00` Toronto | `2024-03-15T12:00` Toronto |
| `2024-03-15T12:00` `America/New_York` | `Europe/London` | `2024-03-15T16:00` London | `2024-03-15T12:00` London |
| `2024-03-15T20:00` `America/New_York` | `Asia/Tokyo` | `2024-03-16T09:00` Tokyo | `2024-03-15T20:00` Tokyo |
