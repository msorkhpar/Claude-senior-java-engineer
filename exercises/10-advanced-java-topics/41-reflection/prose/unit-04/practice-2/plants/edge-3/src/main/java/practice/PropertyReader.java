package practice;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class PropertyReader {

    private final Function<Class<?>, Field[]> scanner;
    private final Map<Class<?>, Map<String, Field>> cache = new ConcurrentHashMap<>();

    public PropertyReader(Function<Class<?>, Field[]> scanner) {
        this.scanner = scanner;
    }

    /** The value of the named field of bean, from a per-class cache filled on first use. */
    public Object read(Object bean, String property) {
        Field field = fieldsOf(bean.getClass()).get(property);
        if (field == null) {
            throw new IllegalArgumentException("no property " + property + " on " + bean.getClass().getName());
        }
        field.setAccessible(true);
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, Field> fieldsOf(Class<?> type) {
        return cache.computeIfAbsent(type, this::scan);
    }

    private Map<String, Field> scan(Class<?> type) {
        Map<String, Field> fields = new HashMap<>();
        for (Field field : scanner.apply(type)) {
            fields.put(field.getName(), field);
        }
        return Map.copyOf(fields);
    }
}
