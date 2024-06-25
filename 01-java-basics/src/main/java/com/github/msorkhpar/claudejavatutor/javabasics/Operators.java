package com.github.msorkhpar.claudejavatutor.javabasics;

public class Operators {

    public static int add(int a, int b) {
        return a + b;
    }

    public static int subtract(int a, int b) {
        return a - b;
    }

    public static int multiply(int a, int b) {
        return a * b;
    }

    public static double divide(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a / b;
    }

    public static int modulus(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Modulus by zero");
        }
        return a % b;
    }

    public static int increment(int a) {
        return ++a;
    }

    public static int decrement(int a) {
        return --a;
    }

    public static boolean isEqual(int a, int b) {
        return a == b;
    }

    public static boolean isNotEqual(int a, int b) {
        return a != b;
    }

    public static boolean isLessThan(int a, int b) {
        return a < b;
    }

    public static boolean isGreaterThanOrEqual(int a, int b) {
        return a >= b;
    }

    public static boolean isLessThanOrEqual(int a, int b) {
        return a <= b;
    }

    public static boolean isGreaterThan(int a, int b) {
        return a > b;
    }

    public static boolean logicalAnd(boolean a, boolean b) {
        return a && b;
    }

    public static boolean logicalOr(boolean a, boolean b) {
        return a || b;
    }

    public static boolean logicalNot(boolean a) {
        return !a;
    }

    public static int bitwiseAnd(int a, int b) {
        return a & b;
    }

    public static int bitwiseOr(int a, int b) {
        return a | b;
    }

    public static int bitwiseXor(int a, int b) {
        return a ^ b;
    }

    public static boolean areFloatsEqual(double a, double b) {
        final double EPSILON = 1e-9;
        return Math.abs(a - b) < EPSILON;
    }
}