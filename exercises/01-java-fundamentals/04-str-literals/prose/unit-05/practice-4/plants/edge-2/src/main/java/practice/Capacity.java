package practice;

public final class Capacity {

    private Capacity() {
    }

    /** Returns an empty builder with room for expectedLength characters. */
    public static StringBuilder forLength(int expectedLength) {
        return new StringBuilder(expectedLength);
    }
}
