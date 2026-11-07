package practice;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/** Writes any object the debug endpoint is asked about, field by field. */
public final class Snapshot {

    private Snapshot() {
    }

    /**
     * Text, numbers, booleans, characters and enum constants as themselves; any other object as
     * {@code Type{field=value, ...}} with its instance fields sorted by name, or as its own
     * {@code toString()} when its fields can't be read.
     */
    public static String render(Object value) {
        if (value == null || value instanceof CharSequence || value instanceof Number
                || value instanceof Boolean || value instanceof Character || value instanceof Enum<?>) {
            return String.valueOf(value);
        }
        Optional<List<Field>> fields = openFields(value.getClass());
        if (fields.isEmpty()) {
            return value.toString();
        }
        List<Field> sorted = new ArrayList<>(fields.get());
        sorted.sort(Comparator.comparing(Field::getName));
        StringJoiner out = new StringJoiner(", ", value.getClass().getSimpleName() + "{", "}");
        for (Field field : sorted) {
            try {
                out.add(field.getName() + "=" + render(field.get(value)));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return out.toString();
    }

    /**
     * The instance fields of {@code type} and of all its superclasses, each ready to be read with
     * {@code get}. Empty when any one of them can't be made readable: the object is then written
     * through its own {@code toString()}.
     */
    static Optional<List<Field>> openFields(Class<?> type) {
        // TODO: collect the instance fields of type and of its superclasses, each made readable.
        //  When one of them can't be made readable, the object is written whole instead: return empty.
        throw new IllegalStateException("TODO: open the fields of " + type.getName());
    }
}
