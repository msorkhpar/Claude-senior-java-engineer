package practice;

import java.util.List;

public final class Values {

    private Values() {
    }

    /** A description of {@code obj} from the first case that matches it. */
    public static String format(Object obj) {
        return switch (obj) {
            case null -> "null";
            case Integer i when i < 0 -> "negative integer " + i;
            case Integer i -> "non-negative integer " + i;
            case String s when s.isBlank() -> "blank string";
            case String s -> "string " + s;
            case List<?> list when list.isEmpty() -> "empty list";
            case List<?> list -> "list of " + list.size();
            default -> "other: " + obj.getClass().getSimpleName();
        };
    }
}
