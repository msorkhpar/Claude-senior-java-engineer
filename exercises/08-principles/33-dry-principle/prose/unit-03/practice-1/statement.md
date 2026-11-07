The conference sells seats in its hands-on workshops in two ways: attendees sign up on the
website, and badges sold by a partner ticketing system are imported in batches. Both run on many
threads at once, and the venue's rule decides how many people a room admits. Last year two
workshops ended up with more attendees than chairs, and the import was blamed in one case and the
sign-up page in the other.

Finish the `TODO` in `src/main/java/practice/Workshops.java`, `tryEnroll(...)`, and change
anything else in the file you need to. When you're done, no workshop gets more attendees than the
venue's rule admits, whether they arrive through the sign-up page, the import, or both at the same
moment. Staff at the door can still add walk-ins over capacity. About six lines in total.

The tests use a venue whose rule can hold its first answer back, so that a second attendee
arrives while the first is still being decided.
