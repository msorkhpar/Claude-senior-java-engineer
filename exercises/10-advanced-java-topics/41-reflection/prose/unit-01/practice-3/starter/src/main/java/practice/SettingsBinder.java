package practice;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Binds configuration values such as {@code ports=8080,8081} into the list fields of a settings
 * object. Each key names a field whose type is a list; the comma-separated items replace the
 * list's contents, each one converted to the list's element type.
 */
public class SettingsBinder {

    public static void bind(Object settings, Map<String, String> values) {
        for (Map.Entry<String, String> e : values.entrySet()) {
            try {
                Field field = settings.getClass().getDeclaredField(e.getKey());
                field.setAccessible(true);
                Class<?> element = elementType(settings, field);
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) field.get(settings);
                list.clear();
                for (String item : e.getValue().split(",")) list.add(convert(item.trim(), element));
            } catch (ReflectiveOperationException ex) {
                throw new IllegalArgumentException("cannot bind " + e.getKey(), ex);
            }
        }
    }

    static Object convert(String item, Class<?> type) {
        if (type == String.class) return item;
        if (type == Integer.class) return Integer.valueOf(item);
        if (type == Long.class) return Long.valueOf(item);
        if (type == Boolean.class) return Boolean.valueOf(item);
        throw new IllegalArgumentException("no converter for " + type);
    }

    /** The element type of the list held by this field of the settings object. */
    static Class<?> elementType(Object settings, Field field) throws ReflectiveOperationException {
        // TODO: The type the items of this field's list must have. A settings class may declare
        //       a field as List<Something>, or as its own class that extends a list of something.
        throw new IllegalStateException("TODO: elementType");
    }
}
