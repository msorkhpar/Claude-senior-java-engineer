package practice;

public final class LineCount {

    private LineCount() {
    }

    /** Returns how many lines {@code value} holds. */
    public static int count(String value) {
        return (int) value.chars().filter(c -> c == '\n').count() + (value.endsWith("\n") ? 0 : 1);
    }
}
