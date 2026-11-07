package practice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Optional;

/** Reads the arrival date a guest typed into the hotel's booking form. */
public class ArrivalDates {

    /** The booking request the form builds: the arrival date, or a message for the guest. */
    public static String bookingRequest(String typed) {
        return arrival(typed)
                .map(date -> "arrival " + date)
                .orElse("please check the arrival date");
    }

    /**
     * The date the guest typed, day.month.year, or empty when the text names no date the
     * calendar has.
     */
    static Optional<LocalDate> arrival(String typed) {
        // TODO: read the date the guest typed (see the statement for what guests type).
        throw new IllegalStateException("TODO: arrival(typed)");
    }
}
