package com.github.msorkhpar.claudejavatutor.literals;

import java.util.List;

public class StringConcatenation {

    public static String simpleConcat(String s1, String s2) {
        return s1 + s2;
    }

    public static String concatWithNull(String s) {
        return s + null;
    }

    public static String concatInLoop(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static String concatWithDifferentTypes(int number, boolean flag, double value) {
        return "Number: " + number + ", Flag: " + flag + ", Value: " + value;
    }

    public static String concatLargeStrings(String s1, String s2, int repeatCount) {
        StringBuilder sb = new StringBuilder(s1.length() * repeatCount + s2.length());
        for (int i = 0; i < repeatCount; i++) {
            sb.append(s1);
        }
        sb.append(s2);
        return sb.toString();
    }
}