package com.github.msorkhpar.claudejavatutor.literals;

public class StringCreation {

    public static String createLiteral() {
        return "Hello, World!";
    }

    public static String createWithNew() {
        return new String("Hello, World!");
    }

    public static String createFromCharArray() {
        char[] charArray = {'H', 'e', 'l', 'l', 'o'};
        return new String(charArray);
    }

    public static String createFromByteArray() {
        byte[] byteArray = {72, 101, 108, 108, 111};
        return new String(byteArray);
    }

    public static String createFromStringBuilder() {
        StringBuilder sb = new StringBuilder("Hello");
        sb.append(", World!");
        return sb.toString();
    }

    public static boolean compareStrings(String s1, String s2) {
        return s1 == s2;
    }

    public static String internString(String s) {
        return s.intern();
    }
}