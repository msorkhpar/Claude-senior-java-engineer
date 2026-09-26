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
            if (e instanceof IllegalArgumentException i) {
                return "bad argument: " + i.getMessage();
            }
            if (e instanceof NumberFormatException n) {
                return "not a number: " + n.getMessage();
            }
            if (e instanceof ArithmeticException a) {
                return "arithmetic: " + a.getMessage();
            }
            return "unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
}
