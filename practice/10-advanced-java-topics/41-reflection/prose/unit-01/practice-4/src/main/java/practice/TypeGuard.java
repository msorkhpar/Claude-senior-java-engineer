package practice;

public final class TypeGuard {

    private TypeGuard() {
    }

    /** Whether value is an instance of type. */
    public static boolean accepts(Class<?> type, Object value) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Whether type is an abstract class; an interface is not one. */
    public static boolean isAbstractClass(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The type's modifiers as source spells them, e.g. "public final". */
    public static String modifiers(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }
}
