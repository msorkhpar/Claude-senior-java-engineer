package practice;

import java.util.Locale;

public record Tag(String value) {

    /** Store the value stripped and in lower case; refuse null and blank values. */
    public Tag {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("A tag needs some text");
        }
    }

    /** Normalised on the way out instead of on the way in. */
    @Override
    public String value() {
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
