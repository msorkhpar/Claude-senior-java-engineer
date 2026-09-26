package practice;

import java.lang.reflect.Modifier;

public final class TypeGuard {

    private TypeGuard() {
    }

    /** Whether value is an instance of type. */
    public static boolean accepts(Class<?> type, Object value) {
        return type.isAssignableFrom(value.getClass());
    }

    /** Whether type is an abstract class; an interface is not one. */
    public static boolean isAbstractClass(Class<?> type) {
        return Modifier.isAbstract(type.getModifiers()) && !type.isInterface();
    }

    /** The type's modifiers as source spells them, e.g. "public final". */
    public static String modifiers(Class<?> type) {
        return Modifier.toString(type.getModifiers());
    }
}
