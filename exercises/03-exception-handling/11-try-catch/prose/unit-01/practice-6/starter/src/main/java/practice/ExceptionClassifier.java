package practice;

public final class ExceptionClassifier {

    private ExceptionClassifier() {
    }

    /** Runs {@code action}; returns "ok", or a description of the RuntimeException it threw. */
    public static String describe(Runnable action) {
        throw new UnsupportedOperationException("write describe");
    }
}
