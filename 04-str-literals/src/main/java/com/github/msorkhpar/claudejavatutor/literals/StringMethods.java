package com.github.msorkhpar.claudejavatutor.literals;

public class StringMethods {

    public static int getStringLength(String str) {
        return str.length();
    }

    public static char getCharAt(String str, int index) {
        return str.charAt(index);
    }

    public static String getSubstring(String str, int beginIndex, int endIndex) {
        return str.substring(beginIndex, endIndex);
    }

    public static String getSubstringToEnd(String str, int beginIndex) {
        return str.substring(beginIndex);
    }

    public static boolean isStringEmpty(String str) {
        return str.isEmpty();
    }


    public static boolean isStringBlank(String str) {
        return str.isBlank();
    }

    public static char getLastChar(String str) {
        if (str.isEmpty()) {
            throw new IllegalArgumentException("String is empty");
        }
        return str.charAt(str.length() - 1);
    }
}
