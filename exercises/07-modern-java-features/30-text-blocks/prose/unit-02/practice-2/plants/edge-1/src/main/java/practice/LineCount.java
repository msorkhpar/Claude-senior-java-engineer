package practice;

public final class LineCount {

    private LineCount() {
    }

    /** Returns how many lines {@code value} holds. */
    public static int count(String value) {
        return value.isEmpty() ? 0 : value.split("\n", -1).length;
    }
}
