package practice;

public final class Nests {

    private Nests() {
    }

    /** The top-level class whose nest {@code c} belongs to. */
    public static Class<?> host(Class<?> c) {
        throw new UnsupportedOperationException("write host");
    }

    /** Whether {@code a} and {@code b} belong to the same nest. */
    public static boolean nestmates(Class<?> a, Class<?> b) {
        throw new UnsupportedOperationException("write nestmates");
    }
}
