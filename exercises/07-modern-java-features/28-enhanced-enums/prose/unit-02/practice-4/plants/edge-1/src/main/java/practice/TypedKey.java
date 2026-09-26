package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class TypedKey<T> {

    private static final List<TypedKey<?>> ALL = new ArrayList<>();

    public static final TypedKey<Integer> PORT = new TypedKey<>("port", 8080);
    public static final TypedKey<String> HOST = new TypedKey<>("host", "0.0.0.0");
    public static final TypedKey<Boolean> VERBOSE = new TypedKey<>("verbose", false);
    public static final TypedKey<List<String>> ALLOWED_ORIGINS = new TypedKey<>("allowed.origins", List.of("*"));

    private final String name;
    private final T defaultValue;

    private TypedKey(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        ALL.add(this);
    }

    public String name() {
        return name;
    }

    public T defaultValue() {
        return defaultValue;
    }

    /** Every key, in declaration order; the caller cannot change the list. */
    public static List<TypedKey<?>> values() {
        return Collections.unmodifiableList(ALL);
    }

    /** The key whose name equals {@code name}, or empty. */
    public static Optional<TypedKey<?>> named(String name) {
        return ALL.stream().filter(k -> k.name == name).findFirst();
    }

    @Override
    public String toString() {
        return "TypedKey[" + name + "]";
    }
}
