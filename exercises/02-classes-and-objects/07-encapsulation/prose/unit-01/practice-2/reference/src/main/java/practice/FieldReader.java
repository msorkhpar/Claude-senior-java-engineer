package practice;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the state of any object for the support team's diagnostics page, which shows every field
 * of an order, a session or a cart next to its value.
 */
public final class FieldReader {

    /** Shown instead of a value this code is not allowed to read. */
    public static final String HIDDEN = "<hidden>";

    private FieldReader() {
    }

    /**
     * Field name to value, for every instance field the object carries. Where two classes of the
     * hierarchy declare the same name, the one found first is kept.
     */
    public static Map<String, Object> read(Object object) {
        Map<String, Object> state = new LinkedHashMap<>();
        for (Field field : instanceFields(object.getClass())) {
            state.putIfAbsent(field.getName(), value(field, object));
        }
        return state;
    }

    /** Every instance field an object of this type carries; static fields are not state. */
    static List<Field> instanceFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) fields.add(field);
            }
        }
        return fields;
    }

    /** The field's value in this object, or HIDDEN when the JVM won't let this code read it. */
    static Object value(Field field, Object object) {
        if (!field.trySetAccessible()) return HIDDEN;
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            return HIDDEN;
        }
    }
}
