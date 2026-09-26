package practice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.TimeZone;

public final class DateInput {

    private DateInput() {
    }

    public static LocalDate parse(String text) {
        // a fresh legacy formatter, left lenient as it comes
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return format.parse(text).toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        } catch (ParseException e) {
            throw new IllegalArgumentException("not a date: " + text, e);
        }
    }
}
