package practice;

public final class ExceptionClassifier {

    private ExceptionClassifier() {
    }

    public static String describe(Runnable action) {
        try {
            action.run();
            return "ok";
        } catch (Throwable e) {
            return switch (e) {
                case NumberFormatException n -> "not a number: " + n.getMessage();
                case IllegalArgumentException i -> "bad argument: " + i.getMessage();
                case ArithmeticException a -> "arithmetic: " + a.getMessage();
                default -> "unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage();
            };
        }
    }
}
