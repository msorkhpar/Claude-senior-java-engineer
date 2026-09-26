package practice;

public final class ClassProbe {

    public enum Outcome { LOADED, NOT_FOUND, INIT_FAILED, UNUSABLE }

    private ClassProbe() {
    }

    /** Loads and initialises {@code name} through {@code loader}, and says how it went. */
    public static Outcome initialise(String name, ClassLoader loader) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Whether {@code name} can be loaded through {@code loader}, without initialising it. */
    public static boolean isPresent(String name, ClassLoader loader) {
        throw new UnsupportedOperationException("TODO");
    }
}
