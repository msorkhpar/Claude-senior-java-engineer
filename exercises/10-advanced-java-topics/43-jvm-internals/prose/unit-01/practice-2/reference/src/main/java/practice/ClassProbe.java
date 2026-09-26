package practice;

public final class ClassProbe {

    public enum Outcome { LOADED, NOT_FOUND, INIT_FAILED, UNUSABLE }

    private ClassProbe() {
    }

    /** Loads and initialises {@code name} through {@code loader}, and says how it went. */
    public static Outcome initialise(String name, ClassLoader loader) {
        try {
            Class.forName(name, true, loader);
            return Outcome.LOADED;
        } catch (ClassNotFoundException e) {
            return Outcome.NOT_FOUND;
        } catch (ExceptionInInitializerError e) {
            return Outcome.INIT_FAILED;
        } catch (NoClassDefFoundError e) {
            return Outcome.UNUSABLE;
        }
    }

    /** Whether {@code name} can be loaded through {@code loader}, without initialising it. */
    public static boolean isPresent(String name, ClassLoader loader) {
        try {
            Class.forName(name, false, loader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
