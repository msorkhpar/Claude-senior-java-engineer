package com.github.msorkhpar.claudejavatutor.literals;

import java.util.Objects;

public class StringComparison {

    public static boolean compareUsingEquals(String str1, String str2) {
        return str1.equals(str2);
    }

    public static boolean compareUsingEqualOperator(String str1, String str2) {
        return str1 == str2;
    }

    public static boolean safeStringCompare(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    public static boolean caseInsensitiveCompare(String str1, String str2) {
        return str1.equalsIgnoreCase(str2);
    }

    public static boolean compareInternedStrings(String str1, String str2) {
        return str1.intern() == str2.intern();
    }
}