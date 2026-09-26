package practice;

public final class Capacity {

    private Capacity() {
    }

    /** Returns an empty builder with room for expectedLength characters. */
    public static StringBuilder forLength(int expectedLength) {
        if (expectedLength < 0) {
            throw new IllegalArgumentException("a length is never negative");
        }
        return new StringBuilder(expectedLength);
    }
}
