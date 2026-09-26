package practice;

public final class Canonical {

    public static final String GREETING = """
            Hello
            World""";

    private static final java.util.Map<String, String> POOL = new java.util.concurrent.ConcurrentHashMap<>(java.util.Map.of(GREETING, GREETING));

    private Canonical() {
    }

    /** Returns the one instance that stands for the characters of {@code text}. */
    public static String of(String text) {
        return POOL.computeIfAbsent(text, t -> t);
    }
}
