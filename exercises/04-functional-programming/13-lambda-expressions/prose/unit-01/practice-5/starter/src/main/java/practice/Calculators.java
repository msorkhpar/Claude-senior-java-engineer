package practice;

public final class Calculators {

    private Calculators() {
    }

    /** The Calculator for {@code operator}: one of + - * /. */
    public static Calculator of(char operator) {
        throw new UnsupportedOperationException("write of");
    }
}

/** A calculation on two ints: one abstract method, so a lambda can implement it. */
@FunctionalInterface
interface Calculator {

    int calculate(int a, int b);

    default int square(int n) {
        return n * n;
    }

    static int add(int a, int b) {
        return a + b;
    }
}

