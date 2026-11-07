package practice;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** The booking form's check-in field. Guests type the date as text, for example 2025-11-04. */
public class CheckInForm {

    /** What the form shows the guest after they leave the field. */
    public static String review(String typed) {
        try {
            LocalDate day = checkInDate(typed);
            return "Check-in on " + day.getDayOfWeek() + " " + day;
        } catch (IllegalArgumentException e) {
            return "Please enter the date as YYYY-MM-DD";
        }
    }

    // TODO: the check-in date the guest typed. The field holds a date exactly as YYYY-MM-DD,
    // with two-digit month and day, and nothing else. Anything else, including a day that does not
    // exist, throws IllegalArgumentException, so that the form asks again instead of booking a
    // different day.
    static LocalDate checkInDate(String typed) {
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd");
        f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        f.setLenient(false);
        java.text.ParsePosition pos = new java.text.ParsePosition(0);
        java.util.Date parsed = f.parse(typed, pos);
        if (parsed == null || pos.getIndex() != typed.length()) {
            throw new IllegalArgumentException("not a date: " + typed);
        }
        return parsed.toInstant().atZone(java.time.ZoneOffset.UTC).toLocalDate();
    }
}
