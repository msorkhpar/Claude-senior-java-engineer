package practice;

public final class Maker {

    private Maker() {
    }

    /** Calls the constructor of type whose parameters fit args, at any access level. */
    public static <T> T make(Class<T> type, Object... args) throws ReflectiveOperationException {
        throw new UnsupportedOperationException("TODO");
    }
}
