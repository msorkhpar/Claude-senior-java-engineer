The hotel's booking form takes the check-in date as typed text. Front-desk staff report guests who
arrive on a day the hotel has no booking for: one typed `2025-09-31` and was booked for
1 October, another pasted `2025-10-12 15:00` from an email. A partner site that sends `2025-7-4`
has asked whether that format is supported; the answer is no, and the form should say so.

Finish `checkInDate(...)` in `src/main/java/practice/CheckInForm.java`. It returns the date when the
field holds exactly one real date written as `YYYY-MM-DD`, and throws `IllegalArgumentException`
for anything else, so that `review(...)` asks the guest again. About five lines.
