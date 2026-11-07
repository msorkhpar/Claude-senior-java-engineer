package practice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Map;

/** Turns one imported row (column name to value) into an object of the requested type. */
public final class RowMapper {

    private RowMapper() {
    }

    /** Builds an object of {@code type} from one row of the import. */
    public static <T> T map(Class<T> type, Map<String, ?> row) {
        try {
            return type.isRecord() ? toRecord(type, row) : toBean(type, row);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot map a row to " + type.getName(), e);
        }
    }

    /** Beans: the no-arg constructor, then every instance field set from the column of its name. */
    static <T> T toBean(Class<T> type, Map<String, ?> row) throws ReflectiveOperationException {
        Constructor<T> noArg = type.getDeclaredConstructor();
        noArg.setAccessible(true);
        T bean = noArg.newInstance();
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            field.set(bean, row.get(field.getName()));
        }
        return bean;
    }

    /**
     * Records: TODO. Build a record of {@code type} from the row. Every check the record makes on
     * its values must still run on imported rows.
     */
    static <T> T toRecord(Class<T> type, Map<String, ?> row) throws ReflectiveOperationException {
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] types = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getType();
        }
        Object[] values = row.values().toArray();
        Constructor<T> canonical = type.getDeclaredConstructor(types);
        canonical.setAccessible(true);
        return canonical.newInstance(values);
    }
}
