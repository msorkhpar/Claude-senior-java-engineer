package practice;

public final class Calculator {

    private Calculator() {
    }

    /** Adds two ints. */
    public static int add(int a, int b) {
        return a + b;
    }

    /** Adds three ints. */
    public static int add(int a, int b, int c) {
        return a + b + c;
    }

    /** Adds two doubles. */
    public static double add(double a, double b) {
        return (int) a + (int) b;
    }

    /** Joins a text and a number, text first. */
    public static String concat(String str, int num) {
        return str + num;
    }

    /** Joins a number and a text, number first. */
    public static String concat(int num, String str) {
        return num + str;
    }
}
