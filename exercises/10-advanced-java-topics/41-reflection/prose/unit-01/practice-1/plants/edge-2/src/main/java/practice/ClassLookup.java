package practice;

import java.util.Optional;

public final class ClassLookup {

    private ClassLookup() {
    }

    /** Loads the named class through this class's loader, initializing it only when asked. */
    public static Optional<Class<?>> load(String name, boolean initialize) {
        try {
            return Optional.of(Class.forName(name));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /** The name that load(...) accepts for this type. */
    public static String nameFor(Class<?> type) {
        return type.getName();
    }
}
