package practice;

public final class ExceptionKinds {

    private ExceptionKinds() {
    }

    /** Whether the compiler forces callers to catch or declare an exception of this type. */
    public static boolean isChecked(Class<? extends Throwable> type) {
        return type != RuntimeException.class
                && type.getSuperclass() != RuntimeException.class
                && !Error.class.isAssignableFrom(type);
    }
}
