package practice;

public final class Nests {

    private Nests() {
    }

    /** The top-level class whose nest {@code c} belongs to. */
    public static Class<?> host(Class<?> c) {
        return c.getDeclaringClass() != null ? c.getDeclaringClass() : c;
    }

    /** Whether {@code a} and {@code b} belong to the same nest. */
    public static boolean nestmates(Class<?> a, Class<?> b) {
        return a.isNestmateOf(b);
    }
}
