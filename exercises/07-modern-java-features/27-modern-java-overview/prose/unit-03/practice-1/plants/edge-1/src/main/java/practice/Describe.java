package practice;

import java.util.List;

public final class Describe {

    private Describe() {
    }

    /** A short description of {@code obj}, by its type and value. */
    public static String describe(Object obj) {
        if (obj instanceof String s && s.length() > 10) {
            return "long string: " + s;
        } else if (obj instanceof String s) {
            return "string of length " + s.length();
        } else if (obj instanceof Integer i && i > 0) {
            return "positive integer " + i;
        } else if (obj instanceof Integer i) {
            return "integer " + i;
        } else if (obj instanceof List<?> list) {
            return "list of " + list.size();
        }
        return "other: " + obj.getClass().getSimpleName();
    }
}
