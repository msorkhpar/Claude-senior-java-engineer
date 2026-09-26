package practice;

public final class Replies {

    public static final String HELLO = """
            Hello
            World""";

    public static final String BYE = """
            Bye""";

    private Replies() {
    }

    /** Returns "greeting" for HELLO, "farewell" for BYE and "unknown" for anything else. */
    public static String reply(String message) {
        return switch (message) {
            case HELLO -> "greeting";
            case BYE -> "farewell";
            default -> "unknown";
        };
    }
}
