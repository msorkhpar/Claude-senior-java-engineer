An `Instant` is a point on the UTC timeline with no calendar and no clock, so a
formatter cannot print it until it knows whose clock to read.

Write `ViewerStamp.format(Instant instant, ZoneId viewer)`. It shows the instant
the way a viewer in `viewer` sees it on their **own clock**, in **English
whatever the server's default locale**, like
`Fri, Mar 15, 2024 11:30 PM EDT`: the short weekday, the short month, the day,
the year, the time on the **12-hour clock** with `AM` or `PM`, and the zone's
short name.

| instant | viewer | answer |
|---|---|---|
| `2024-01-15T14:30:00Z` | `UTC` | `Mon, Jan 15, 2024 2:30 PM UTC` |
| `2024-01-15T14:30:00Z` | `Europe/London` | `Mon, Jan 15, 2024 2:30 PM GMT` |
| `2024-03-16T03:30:00Z` | `America/New_York` | `Fri, Mar 15, 2024 11:30 PM EDT` |
| `2024-01-15T14:30:00Z` | `Asia/Tokyo` | `Mon, Jan 15, 2024 11:30 PM JST` |
| `2024-01-15T00:15:00Z` | `UTC` | `Mon, Jan 15, 2024 12:15 AM UTC` |
