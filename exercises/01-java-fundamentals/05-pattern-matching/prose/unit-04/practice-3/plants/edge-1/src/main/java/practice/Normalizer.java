package practice;

public final class Normalizer {

    private Normalizer() {
    }

    /** Returns a trimmed String, an Integer's digits, or "". */
    public static String text(Object obj) {
        return switch (obj) {
            case String s -> s.trim();
            case Integer i -> Integer.toString(i);
            default -> "";
        };
    }
}
