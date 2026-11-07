The scheduling service stores each meeting as a `ZonedDateTime` in the zone of the office that
hosts it. Two tickets are open. Attendees in other offices say the invite email shows the meeting
at the wrong time for them; one in Toronto joined a Madrid meeting long after it had ended. And
when an organiser hands a recurring 16:30 meeting to the office in another country, the new office
finds it at some other hour of their day.

Finish the two `TODO` methods in `src/main/java/practice/Invites.java`:

- `asSeenBy(meeting, attendeeZone)`: the meeting as an attendee living in `attendeeZone` finds it
  in their calendar;
- `moveToOffice(meeting, officeZone)`: the meeting once the office in `officeZone` hosts it, at
  the same time on its clocks as before.

`inviteLines` already builds the email from `asSeenBy`. About two lines in total.
