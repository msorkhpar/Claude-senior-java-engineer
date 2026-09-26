The page's rule is to **convert at the boundary**: `java.time` inside, legacy
types only where an old API demands them. An old job scheduler takes and returns
`java.util.Date`:

```java
public interface LegacyScheduler {
    void schedule(String job, Date when); // keeps the Date object it is given
    Date nextRun(String job);             // null when it has no run for the job
}
```

Write `SchedulerAdapter`, which wraps a `LegacyScheduler` so that the rest of
the code only sees `java.time`:

- `schedule(String job, ZonedDateTime when)` schedules the job at that moment,
  to the millisecond, whatever zone `when` is written in.
  The legacy scheduler **keeps** the `Date` it receives, so every call must hand
  it a `Date` **of its own**.
- `nextRun(String job, ZoneId zone)` returns the job's next run as a
  `ZonedDateTime` in **the zone asked for**, or an **empty** `Optional` when the
  legacy scheduler returns `null`.

| steps | answer |
|---|---|
| `schedule("backup", 2024-03-15T10:00Z)`, then `nextRun("backup", UTC)` | `Optional[2024-03-15T10:00Z]` |
| `schedule("a", 10:00Z)`, `schedule("b", 11:00Z)`, then `nextRun("a", UTC)` | `Optional[...T10:00Z]` |
| `schedule("tokyo", 2024-03-15T19:00+09:00[Asia/Tokyo])`, then `nextRun("tokyo", UTC)` | `Optional[2024-03-15T10:00Z]` |
| `schedule("precise", 2024-03-15T10:00:00.250Z)`, then `nextRun("precise", UTC)` | `Optional[2024-03-15T10:00:00.250Z]` |
| `nextRun("unknown", UTC)` | `Optional.empty` |
| `schedule("backup", 2024-03-15T10:00Z)`, then `nextRun("backup", Asia/Tokyo)` | `Optional[2024-03-15T19:00+09:00[Asia/Tokyo]]` |
