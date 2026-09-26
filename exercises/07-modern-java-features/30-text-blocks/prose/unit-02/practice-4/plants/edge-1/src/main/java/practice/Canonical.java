package practice;

public final class Canonical {

    public static final String GREETING = """
            Hello
            World""";

    private Canonical() {
    }

    /** Returns the one instance that stands for the characters of {@code text}. */
    public static String of(String text) {
        return text; // strings are already interned
    }
}
