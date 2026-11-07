package practice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Map;

/** Turns one imported row, a map from column name to value, into a DTO. */
public final class RowBinder {

    private RowBinder() {}

    public static <T> T bind(Class<T> type, Map<String, Object> row) throws ReflectiveOperationException {
        if (type.isRecord()) {
            return bindRecord(type, row);
        }
        // A JavaBean: no-arg constructor, then one setter per column.
        T bean = type.getDeclaredConstructor().newInstance();
        for (Map.Entry<String, Object> column : row.entrySet()) {
            String setter = "set" + Character.toUpperCase(column.getKey().charAt(0)) + column.getKey().substring(1);
            for (Method m : type.getMethods()) {
                if (m.getName().equals(setter) && m.getParameterCount() == 1) {
                    m.invoke(bean, column.getValue());
                    break;
                }
            }
        }
        return bean;
    }

    static <T> T bindRecord(Class<T> type, Map<String, Object> row) throws ReflectiveOperationException {
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] types = new Class<?>[components.length];
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getType();
            args[i] = row.get(components[i].getName());
        }
        try {
            return type.getDeclaredConstructor(types).newInstance(args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException refused) {
                throw refused;
            }
            throw e;
        }
    }
}
