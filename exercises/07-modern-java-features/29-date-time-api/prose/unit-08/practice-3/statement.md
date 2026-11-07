The courier service books pickups through a dispatch library written before Java 8. It takes the
requested pickup as a `GregorianCalendar`, and returns the confirmed pickup as a `Calendar` in the
depot's zone. Each depot is configured with a zone ID. Last month a depot was configured with a
misspelled ID and its pickups were booked hours off for two weeks before anyone noticed, and the
drivers' app sometimes shows a confirmed pickup at a time that is right on no clock at the depot.

`DispatchAdapter.book(...)` already calls the library. Finish the three `TODO` methods in
`src/main/java/practice/DispatchAdapter.java`:

- `depotZone(configuredId)`: the depot's zone for the library, refusing an ID that names no zone;
- `toLibrary(pickup)`: the calendar to hand the library;
- `fromLibrary(returned)`: the confirmed pickup, as the moment the calendar holds in its own zone.

The servers' default zone must make no difference; the tests use depots in several zones and
never change it. About three lines in total.
