package practice;

public final class LineCount {

    private LineCount() {
    }

    /** Returns how many lines {@code value} holds. */
    public static int count(String value) {
        return value.split("\n").length;
    }
}
