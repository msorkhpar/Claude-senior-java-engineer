package com.github.msorkhpar.claudejavatutor.javabasics;

public class TypeCasting {

    public long implicitIntToLong(int value) {
        return value; // Implicit casting from int to long
    }

    public double implicitIntToDouble(int value) {
        return value; // Implicit casting from int to double
    }

    public int explicitDoubleToInt(double value) {
        return (int) value; // Explicit casting from double to int
    }

    public int explicitLongToInt(long value) {
        return (int) value; // Explicit casting from long to int
    }

    public Integer intToInteger(int value) {
        return value; // Auto-boxing
    }

    public Character charToCharacter(char value) {
        return value; // Auto-boxing
    }

    public int integerToInt(Integer value) {
        return value; // Auto-unboxing
    }

    public char characterToChar(Character value) {
        return value; // Auto-unboxing
    }

    public int stringToInt(String value) {
        return Integer.parseInt(value);
    }

    public String intToString(int value) {
        return String.valueOf(value);
    }

    public int demonstrateDataLoss(double value) {
        return (int) value; // Potential loss of fractional part
    }

    public int demonstrateOverflow(long value) {
        return (int) value; // Potential overflow
    }
}