A parcel service takes delivery events from partner depots around the world and stores each one
as an `Instant`. Each depot sends its local time with its offset from UTC, in the ISO form:

```text
2031-11-14T15:00:00+05:30
2031-11-14T06:30:00-03:00
2031-11-14T09:30:00Z
```

These three are the same moment. When a support agent opens a parcel's page, its history shows each
event as the agent's own clock read it, like `2031-11-14 15:00` for an agent in Asia/Kolkata. The
service runs on servers set to UTC; its agents are not.

Finish the two `TODO`s in `src/main/java/practice/PartnerEvents.java`:

- `receivedAt(...)`: the moment a depot's event time names;
- `forAgent(...)`: that moment as the agent's clock showed it, to the minute.

About two lines in total.
