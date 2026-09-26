package practice;

import java.lang.reflect.Field;

public final class FieldAccess {

    private FieldAccess() {
    }

    /** The value of the named field of target, declared on its class or any superclass. */
    public static Object read(Object target, String name) throws ReflectiveOperationException {
        Field field = find(target.getClass(), name);
        return field == null ? null : field.get(target);
    }

    /** Sets the named field of target, declared on its class or any superclass. */
    public static void write(Object target, String name, Object value) throws ReflectiveOperationException {
        find(target.getClass(), name).set(target, value);
    }

    /** The value of a static field of type. */
    public static Object readStatic(Class<?> type, String name) throws ReflectiveOperationException {
        return find(type, name).get(null);
    }

    private static Field find(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
