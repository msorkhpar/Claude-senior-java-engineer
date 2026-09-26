package practice;

public final class Mixed {

    private Mixed() {
    }

    /** Describes three values, as the course does. */
    public static String describe(int number, boolean flag, double value) {
        return "Number: " + number + ", Flag: " + flag + ", Value: " + value;
    }

    /** Returns the label, a colon and the sum of a and b. */
    public static String total(String label, int a, int b) {
        return label + ": " + (a + b);
    }

    /** Returns the two initials side by side. */
    public static String initials(char first, char last) {
        return first + last + "";
    }
}
