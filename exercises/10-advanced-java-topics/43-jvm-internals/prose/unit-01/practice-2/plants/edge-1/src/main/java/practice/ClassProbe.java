package practice;

public final class ClassProbe {

    public enum Outcome { LOADED, NOT_FOUND, INIT_FAILED, UNUSABLE }

    private ClassProbe() {
    }

    // Treats every LinkageError from initialization as the first failure.
    public static Outcome initialise(String name, ClassLoader loader) {
        try {
            Class.forName(name, true, loader);
            return Outcome.LOADED;
        } catch (ClassNotFoundException e) {
            return Outcome.NOT_FOUND;
        } catch (LinkageError e) {
            return Outcome.INIT_FAILED;
        }
    }

    public static boolean isPresent(String name, ClassLoader loader) {
        try {
            Class.forName(name, false, loader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
