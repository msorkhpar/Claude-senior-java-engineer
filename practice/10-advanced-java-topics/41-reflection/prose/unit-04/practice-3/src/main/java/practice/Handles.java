package practice;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public final class Handles {

    private Handles() {
    }

    /** A handle for an instance method of any access level, receiver first. */
    public static MethodHandle virtual(Class<?> owner, String name, MethodType type)
            throws ReflectiveOperationException {
        throw new UnsupportedOperationException("TODO");
    }

    /** A handle for a static method of any access level. */
    public static MethodHandle staticMethod(Class<?> owner, String name, MethodType type)
            throws ReflectiveOperationException {
        throw new UnsupportedOperationException("TODO");
    }

    /** A handle that reads an instance field of any access level. */
    public static MethodHandle getter(Class<?> owner, String field, Class<?> fieldType)
            throws ReflectiveOperationException {
        throw new UnsupportedOperationException("TODO");
    }
}
