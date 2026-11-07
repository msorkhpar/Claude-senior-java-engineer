package practice;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

/**
 * Backs the support team's /debug/dump endpoint: it shows the instance fields of whatever object
 * the support engineer points it at, the service's own objects and the JDK objects they hold.
 */
public class FieldDump {

    /** What the dump shows for a field the JVM won't let it read. */
    public static final String HIDDEN = "<hidden>";

    /** Every instance field declared by the object's class, by name, with its value as text. */
    public static Map<String, String> dump(Object target) {
        Map<String, String> fields = new TreeMap<>();
        for (Field field : target.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            fields.put(field.getName(), read(field, target));
        }
        return fields;
    }

    /**
     * The field's value in target as text, or HIDDEN when the JVM won't let the dump read it.
     * TODO: read the field, private ones included, without letting a refusal reach the caller.
     */
    static String read(Field field, Object target) {
        throw new IllegalStateException("TODO: read the field, or return HIDDEN");
    }
}
