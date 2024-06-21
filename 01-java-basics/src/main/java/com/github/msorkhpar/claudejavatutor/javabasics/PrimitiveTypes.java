package com.github.msorkhpar.claudejavatutor.javabasics;

public class PrimitiveTypes {

    public byte[] getByteRange() {
        return new byte[]{Byte.MIN_VALUE, Byte.MAX_VALUE};
    }

    public short[] getShortRange() {
        return new short[]{Short.MIN_VALUE, Short.MAX_VALUE};
    }

    public int[] getIntRange() {
        return new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE};
    }

    public long[] getLongRange() {
        return new long[]{Long.MIN_VALUE, Long.MAX_VALUE};
    }

    public float[] getFloatRange() {
        return new float[]{Float.MIN_VALUE, Float.MAX_VALUE};
    }

    public double[] getDoubleRange() {
        return new double[]{Double.MIN_VALUE, Double.MAX_VALUE};
    }

    public boolean[] getBooleanValues() {
        return new boolean[]{false, true};
    }

    public char[] getCharRange() {
        return new char[]{Character.MIN_VALUE, Character.MAX_VALUE};
    }

    public int demonstrateIntegerOverflow() {
        return Integer.MAX_VALUE + 1;
    }

    public boolean demonstrateFloatingPointPrecision() {
        return 0.1 + 0.2 == 0.3;
    }

    public boolean compareFloatingPoint(double a, double b, double expected) {
        final double EPSILON = 1e-9;
        return Math.abs((a + b) - expected) < EPSILON;
    }

    public void demonstrateDivisionByZero() {
        int result = 5 / 0;
    }

    public char demonstrateCharOperations() {
        char a = 'a';
        return (char) (a + 1);
    }
}