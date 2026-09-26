package practice;

public final class ClassProbe {

    public enum Outcome { LOADED, NOT_FOUND, INIT_FAILED, UNUSABLE }

    private ClassProbe() {
    }

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

    // Reuses initialise(), so presence runs the static initializer.
    public static boolean isPresent(String name, ClassLoader loader) {
        return initialise(name, loader) != Outcome.NOT_FOUND;
    }
}
