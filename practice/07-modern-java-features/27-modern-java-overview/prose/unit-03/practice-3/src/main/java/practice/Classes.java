package practice;

public final class Classes {

    private Classes() {
    }

    /** "hidden", "anonymous" or "named". */
    public static String kind(Class<?> c) {
        throw new UnsupportedOperationException("write kind");
    }

    /** Whether looking the class up by its name finds that same class. */
    public static boolean loadable(Class<?> c) {
        throw new UnsupportedOperationException("write loadable");
    }
}
