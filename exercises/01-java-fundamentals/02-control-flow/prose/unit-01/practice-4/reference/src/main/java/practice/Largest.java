package practice;

public final class Largest {

    private Largest() {
    }

    /** Returns the largest of the three values. */
    public static int max(int a, int b, int c) {
        int ab = a > b ? a : b;
        return ab > c ? ab : c;
    }

    /** Returns "Adult" for an age of 18 or more, and "Minor" otherwise. */
    public static String status(int age) {
        return age >= 18 ? "Adult" : "Minor";
    }
}
