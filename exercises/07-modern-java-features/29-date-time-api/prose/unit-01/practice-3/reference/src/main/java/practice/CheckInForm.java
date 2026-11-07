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
        try {
            return LocalDate.parse(typed);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("not a date: " + typed, e);
        }
    }
}
