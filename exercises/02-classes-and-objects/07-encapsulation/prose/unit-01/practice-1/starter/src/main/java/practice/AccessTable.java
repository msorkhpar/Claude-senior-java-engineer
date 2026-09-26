package practice;

public final class AccessTable {

    /** The four access levels. */
    public enum Level { PUBLIC, PROTECTED, PACKAGE, PRIVATE }

    /** Where the code that uses a member sits, relative to the member's declaring class. */
    public enum From { SAME_CLASS, NESTED_CLASS, SAME_PACKAGE, SUBCLASS_OTHER_PACKAGE, OTHER_PACKAGE }

    private AccessTable() {
    }

    /** Whether code at {@code from} may use a member declared with {@code level}. */
    public static boolean canAccess(Level level, From from) {
        throw new UnsupportedOperationException("write canAccess");
    }
}
