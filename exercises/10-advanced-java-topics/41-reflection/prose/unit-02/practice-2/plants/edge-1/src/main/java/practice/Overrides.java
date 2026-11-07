package practice;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Lets a test replace fields of the object under test: every field it names, or none of them. */
public final class Overrides {

    private Overrides() {
    }

    /**
     * Sets each named field of {@code target} to its new value. A field is looked up on the
     * target's own class, at any access level; a static field is set on the class. When any named
     * field can't be written, nothing is written and the call fails with an
     * {@link IllegalArgumentException} naming that field.
     */
    public static void apply(Object target, Map<String, ?> values) {
        List<Field> fields = new ArrayList<>();
        for (String name : values.keySet()) {
            Field field;
            try {
                field = target.getClass().getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("no field " + name + " on " + target.getClass().getSimpleName());
            }
            if (!writable(field)) {
                throw new IllegalArgumentException("can't override " + name + " on " + target.getClass().getSimpleName());
            }
            fields.add(field);
        }
        for (Field field : fields) {
            field.setAccessible(true);
            Object owner = Modifier.isStatic(field.getModifiers()) ? null : target;
            try {
                field.set(owner, values.get(field.getName()));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("override failed half-way, at " + field.getName(), e);
            }
        }
    }

    /** Whether {@code Field.set} will write {@code field} once it has been made accessible. */
    static boolean writable(Field field) {
        int modifiers = field.getModifiers();
        if (!Modifier.isFinal(modifiers)) return true;
        return !field.getDeclaringClass().isRecord();
    }
}
