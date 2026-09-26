package practice;

import java.util.Locale;

public record Tag(String value) {

    /** Store the value stripped and in lower case; refuse null and blank values. */
    public Tag {
        value = value.strip().toLowerCase(Locale.ROOT);
        if (value.isEmpty()) {
            throw new IllegalArgumentException("A tag needs some text");
        }
    }
}
