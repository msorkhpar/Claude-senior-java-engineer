package practice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps the "type" field of an incoming webhook payload to the record class the payload is
 * decoded into: "delivered" to Delivered, for example.
 */
public final class PayloadTypes {

    private PayloadTypes() {
    }

    /**
     * Every record class a value of {@code root} can be, keyed by its simple name in lower case.
     * The decoder builds this once at startup and rejects any payload whose type isn't a key.
     */
    public static Map<String, Class<?>> of(Class<?> root) {
        Map<String, Class<?>> types = new HashMap<>();
        collect(root, types);
        return Map.copyOf(types);
    }

    /** Registers one payload record under its key. */
    static void register(Class<?> record, Map<String, Class<?>> types) {
        types.put(record.getSimpleName().toLowerCase(Locale.ROOT), record);
    }

    /**
     * Adds to {@code types} every record class a value of {@code type} can be. The map must be
     * complete: when that can't be known from {@code type}, throw IllegalArgumentException.
     */
    static void collect(Class<?> type, Map<String, Class<?>> types) {
        if (type.isRecord()) {
            register(type, types);
            return;
        }
        Class<?>[] permitted = type.getPermittedSubclasses();
        if (permitted == null) {
            throw new IllegalArgumentException(type.getName() + " is not sealed");
        }
        for (Class<?> subtype : permitted) {
            collect(subtype, types);
        }
    }
}
