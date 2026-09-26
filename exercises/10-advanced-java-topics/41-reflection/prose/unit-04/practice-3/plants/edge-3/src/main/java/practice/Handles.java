package practice;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class Handles {

    private Handles() {
    }

    /** A handle for an instance method of any access level, receiver first. */
    public static MethodHandle virtual(Class<?> owner, String name, MethodType type)
            throws ReflectiveOperationException {
        return lookupFor(owner).findVirtual(owner, name, type);
    }

    /** A handle for a static method of any access level. */
    public static MethodHandle staticMethod(Class<?> owner, String name, MethodType type)
            throws ReflectiveOperationException {
        return lookupFor(owner).findStatic(owner, name, type);
    }

    /** A handle that reads an instance field of any access level. */
    public static MethodHandle getter(Class<?> owner, String field, Class<?> fieldType)
            throws ReflectiveOperationException {
        return lookupFor(owner).findGetter(owner, field, fieldType);
    }

    private static MethodHandles.Lookup lookupFor(Class<?> owner) throws IllegalAccessException {
        return MethodHandles.lookup();
    }
}
