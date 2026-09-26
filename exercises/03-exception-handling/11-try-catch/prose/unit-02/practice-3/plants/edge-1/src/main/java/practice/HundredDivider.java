package practice;

public final class HundredDivider {

    private HundredDivider() {
    }

    /** Returns a one-line report of dividing 100 by the int in {@code input}. */
    public static String describe(String input) {
        try {
            int value = Integer.parseInt(input);
            if (value < 0) {
                throw new IllegalArgumentException("Value must be non-negative");
            }
            return "Result: " + (100 / value);
        } catch (IllegalArgumentException | ArithmeticException e) {
            return "Invalid input: " + e.getMessage();
        }
    }
}
