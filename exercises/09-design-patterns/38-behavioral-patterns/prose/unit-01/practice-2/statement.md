The admin page lists user sessions, the most recently seen first. Sessions last seen at the same
millisecond, which happens when a batch job touches many at once, are listed by id from A to Z
so the page doesn't reshuffle on every refresh. The list has looked right on staging, where every
session is a few hours old. In production, sessions are kept for six months.

Write `newestFirst()` in `src/main/java/practice/Sessions.java`: the comparator that
`forAdminPage` sorts with. `lastSeenMillis` is milliseconds since the epoch. About two lines in
total.
