package practice;

public final class ExceptionKinds {

    private ExceptionKinds() {
    }

    /** Whether the compiler forces callers to catch or declare an exception of this type. */
    public static boolean isChecked(Class<? extends Throwable> type) {
        return !RuntimeException.class.isAssignableFrom(type);
    }
}
