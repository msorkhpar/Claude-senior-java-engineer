package practice;

import java.util.Optional;
import java.util.function.Supplier;

/** A member; nick may be null. */
record Member(String name, String nick) {

    Optional<String> nickname() {
        return Optional.ofNullable(nick);
    }
}

public final class Lookups {

    private Lookups() {
    }

    /** The member's nickname upper-cased, or empty. */
    public static Optional<String> nickname(Optional<Member> member) {
        throw new UnsupportedOperationException("write nickname");
    }

    /** The length of the trimmed text, or empty when there is none or it is blank. */
    public static Optional<Integer> trimmedLength(Optional<String> text) {
        throw new UnsupportedOperationException("write trimmedLength");
    }

    /** The value, or a new one from factory when it is missing. */
    public static <T> T orCreate(Optional<T> value, Supplier<T> factory) {
        throw new UnsupportedOperationException("write orCreate");
    }
}
