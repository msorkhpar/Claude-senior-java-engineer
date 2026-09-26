package practice;

import java.util.Locale;

public record Tag(String value) {

    /** Store the value stripped and in lower case; refuse null and blank values. */
    public Tag {
        throw new UnsupportedOperationException("write the compact constructor");
    }
}
