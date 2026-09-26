package practice;

public final class Invoker {

    private Invoker() {
    }

    /** Invokes the method with these exact parameter types on target. */
    public static Object call(Object target, String name, Class<?>[] types, Object... args)
            throws ReflectiveOperationException {
        throw new UnsupportedOperationException("TODO");
    }

    /** Invokes the static method with these exact parameter types on type. */
    public static Object callStatic(Class<?> type, String name, Class<?>[] types, Object... args)
            throws ReflectiveOperationException {
        throw new UnsupportedOperationException("TODO");
    }
}
