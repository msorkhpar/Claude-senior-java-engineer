package practice;

public final class FieldAccess {

    private FieldAccess() {
    }

    /** The value of the named field of target, declared on its class or any superclass. */
    public static Object read(Object target, String name) throws ReflectiveOperationException {
        throw new UnsupportedOperationException("TODO");
    }

    /** Sets the named field of target, declared on its class or any superclass. */
    public static void write(Object target, String name, Object value) throws ReflectiveOperationException {
        throw new UnsupportedOperationException("TODO");
    }

    /** The value of a static field of type. */
    public static Object readStatic(Class<?> type, String name) throws ReflectiveOperationException {
        throw new UnsupportedOperationException("TODO");
    }
}
