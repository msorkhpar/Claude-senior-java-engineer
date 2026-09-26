package practice;

public final class Classes {

    private Classes() {
    }

    /** "hidden", "anonymous" or "named". */
    public static String kind(Class<?> c) {
        if (c.isHidden()) {
            return "hidden";
        }
        if (c.isAnonymousClass()) {
            return "anonymous";
        }
        return "named";
    }

    /** Whether looking the class up by its name finds that same class. */
    public static boolean loadable(Class<?> c) {
        return !c.isAnonymousClass();
    }
}
