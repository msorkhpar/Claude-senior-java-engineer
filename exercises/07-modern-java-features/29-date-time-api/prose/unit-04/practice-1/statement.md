A parcel's tracking page merges three feeds. The line-haul carrier posts JSON such as
`{"event": "picked up", "sentAt": 1795300000}`, the courier sends ISO-8601 text with a `Z` or an
offset, and the sorting depot's scanners report what their wall clock showed when the parcel went
past, such as `2026-11-19T08:20:00`. Customers of the Chicago depot see their parcel arrive at the
depot hours before the truck that brought it left, and support keeps finding line-haul events
dated 1970.

`ParcelTimeline` already merges and sorts the feeds, and reads the courier's text. Finish the two
`TODO` methods in `src/main/java/practice/ParcelTimeline.java`:

- `fromSentAt(long)`: the moment a line-haul `sentAt` value names. The carrier's API documentation
  says it counts whole seconds since 1970-01-01T00:00Z.
- `fromScanner(String)`: the moment at which a scanner of this depot showed the given reading. The
  depot's zone is passed to the constructor.

When you're done, every event sits at the moment it happened, wherever its feed came from. About
two lines in total.
