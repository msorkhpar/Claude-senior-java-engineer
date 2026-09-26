package practice;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public final class FieldAudit {

    private FieldAudit() {
    }

    /** The sorted names of the fields {@code type} declares that are neither private nor constants. */
    public static List<String> exposed(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> !field.isSynthetic())
                .filter(field -> !Modifier.isPrivate(field.getModifiers()))
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(Field::getName)
                .sorted()
                .toList();
    }
}
