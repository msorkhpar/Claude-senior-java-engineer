package practice;

import java.util.List;

public final class Describe {

    private Describe() {
    }

    /** Describes an object by its type, and an Integer by its sign. */
    public static String describe(Object obj) {
        return switch (obj) {
            case null -> "Null input";
            case String s -> "String of length " + s.length();
            case Integer i when i > 0 -> "Positive integer";
            case Integer i -> "Non-positive integer";
            case List<?> list -> "List with " + list.size() + " elements";
            default -> "Something else";
        };
    }
}
