A chain of gyms publishes its weekly classes through a public API, which sends each session as an
ISO-8601 date-time with an offset, such as `2026-06-03T18:00+10:00`. A class is set up with its
first session, a `ZonedDateTime` in the gym's own zone, and meets every week at the same time on
that gym's clocks.

Members complain twice a year that the app shows classes an hour off, and the early-morning
classes of one gym stayed an hour late for the rest of the season after one night in March.

Finish `sessions` in `src/main/java/practice/WeeklyFeed.java`: the first `count` sessions,
starting with the first one. When a session's time doesn't exist that week because the clocks
skip it, the session is held at the time the zone moves it to, and the weeks after it are not
affected. About five lines in total.
