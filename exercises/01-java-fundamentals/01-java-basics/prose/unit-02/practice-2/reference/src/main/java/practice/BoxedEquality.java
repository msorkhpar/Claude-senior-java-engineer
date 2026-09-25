package practice;

import java.util.Objects;

public final class BoxedEquality {

    private BoxedEquality() {
    }

    /** Returns whether {@code a} and {@code b} hold the same number, or are both null. */
    public static boolean sameValue(Integer a, Integer b) {
        return Objects.equals(a, b);
    }
}
