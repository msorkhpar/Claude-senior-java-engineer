package practice;

import java.util.Optional;

public final class ClassLookup {

    private ClassLookup() {
    }

    /** Loads the named class through this class's loader, initializing it only when asked. */
    public static Optional<Class<?>> load(String name, boolean initialize) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The name that load(...) accepts for this type. */
    public static String nameFor(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }
}
