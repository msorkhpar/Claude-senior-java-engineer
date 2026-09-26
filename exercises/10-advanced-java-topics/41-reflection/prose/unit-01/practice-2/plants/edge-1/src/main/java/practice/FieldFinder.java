package practice;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FieldFinder {

    private FieldFinder() {
    }

    /** The names of the fields this type declares itself, any access level, sorted. */
    public static List<String> ownFields(Class<?> type) {
        return names(type.getFields());
    }

    /** The names of every public field, inherited ones included, sorted. */
    public static List<String> publicFields(Class<?> type) {
        return names(type.getFields());
    }

    /** The names of every field of this type and its superclasses: own first, each class sorted. */
    public static List<String> allFields(Class<?> type) {
        List<String> all = new ArrayList<>();
        Class<?> current = type;
        while (current != null) {
            all.addAll(names(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return all;
    }

    private static List<String> names(Field[] fields) {
        return Arrays.stream(fields).map(Field::getName).sorted().toList();
    }
}
