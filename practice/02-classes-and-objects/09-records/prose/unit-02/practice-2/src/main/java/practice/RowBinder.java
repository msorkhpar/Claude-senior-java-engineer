package practice;

import java.lang.reflect.Method;
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
        // TODO: build the record from the row: each component takes the column with its name.
        // When the record refuses a value, the caller must get the exception the record threw.
        throw new IllegalStateException("TODO: bindRecord");
    }
}
