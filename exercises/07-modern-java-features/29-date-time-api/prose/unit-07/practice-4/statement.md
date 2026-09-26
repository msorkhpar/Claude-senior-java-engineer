A weekly meeting is set up in its organiser's zone, and the public API sends
each occurrence as an `OffsetDateTime`, which carries a plain UTC offset and no
zone rules.

Write `Recurring.occurrences(ZonedDateTime first, int count)`: the first `count`
occurrences, one week apart, starting with `first` itself, offset included. The meeting keeps
its wall-clock time in its zone, so **the offset changes** when daylight saving
starts or ends. Occurrence `k` is `k` weeks after `first`; when that week's
time does not exist, because the clocks skip it, the meeting is held at the
time the zone moves it to, and the weeks after are not affected.

| first (America/New_York) | count | answer |
|---|---|---|
| `2024-01-05T09:00` | 3 | `2024-01-05T09:00-05:00`, `2024-01-12T09:00-05:00`, `2024-01-19T09:00-05:00` |
| `2024-03-01T09:00` | 3 | `2024-03-01T09:00-05:00`, `2024-03-08T09:00-05:00`, `2024-03-15T09:00-04:00` |
| `2024-10-27T09:00` | 2 | `2024-10-27T09:00-04:00`, `2024-11-03T09:00-05:00` |
| `2024-11-03T01:30-05:00` (the second 01:30) | 2 | `2024-11-03T01:30-05:00`, `2024-11-10T01:30-05:00` |
| `2024-03-03T02:30` | 3 | `2024-03-03T02:30-05:00`, `2024-03-10T03:30-04:00`, `2024-03-17T02:30-04:00` |
