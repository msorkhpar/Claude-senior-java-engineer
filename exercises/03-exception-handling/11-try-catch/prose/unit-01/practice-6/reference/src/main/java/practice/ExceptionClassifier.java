package practice;

public final class ExceptionClassifier {

    private ExceptionClassifier() {
    }

    /** Runs {@code action}; returns "ok", or a description of the RuntimeException it threw. */
    public static String describe(Runnable action) {
        try {
            action.run();
            return "ok";
        } catch (RuntimeException e) {
            return switch (e) {
                case NumberFormatException n -> "not a number: " + n.getMessage();
                case IllegalArgumentException i -> "bad argument: " + i.getMessage();
                case ArithmeticException a -> "arithmetic: " + a.getMessage();
                default -> "unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage();
            };
        }
    }
}
