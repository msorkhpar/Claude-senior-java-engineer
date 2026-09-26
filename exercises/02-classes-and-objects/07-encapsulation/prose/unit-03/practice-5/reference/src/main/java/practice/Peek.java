package practice;

import java.lang.reflect.Field;

public final class Peek {

    private Peek() {
    }

    /** The value of {@code target}'s field {@code fieldName}, private or not. */
    public static Object read(Object target, String fieldName) {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException notHere) {
                // keep looking in the superclass
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("No field " + fieldName + " in " + target.getClass().getName());
    }
}
