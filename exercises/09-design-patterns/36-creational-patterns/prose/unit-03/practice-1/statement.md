The meeting-room calendar has bookings that end before they start, and a few with no organiser,
so nobody can be asked to free the room. They all came from the booking form, which creates a
`Booking` through `Booking.builder()`: it calls a setter every time the user fills in or changes a
field, in whatever order the user goes, and calls `build()` when the user presses Save.

Finish `build()` in `src/main/java/practice/Booking.java`. Every booking needs a room, an
organiser, a start and an end, and must end after it starts. Refuse any other with
`Booking.InvalidBooking`, whose message the form shows next to the fields; a booking that can take
place is created as the user left it. About eight lines in total.
