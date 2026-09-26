package practice;

public final class Describer {

    private Describer() {
    }

    /** Describes a String by its length, null as nothing, and anything else as not text. */
    public static String describe(Object obj) {
        if (obj instanceof String s) {
            return "text of " + s.length();
        }
        return "not text";
    }
}
