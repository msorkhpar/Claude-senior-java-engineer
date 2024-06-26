package com.github.msorkhpar.claudejavatutor.literals;

public class StringBuilderBuffer {

    public static String useStringBuilder(String... strings) {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static String useStringBuffer(String... strings) {
        StringBuffer sb = new StringBuffer();
        for (String s : strings) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static String reverseWithStringBuilder(String input) {
        return new StringBuilder(input).reverse().toString();
    }

    public static String insertWithStringBuffer(String original, String toInsert, int position) {
        StringBuffer sb = new StringBuffer(original);
        return sb.insert(position, toInsert).toString();
    }

    public static String deleteWithStringBuilder(String original, int start, int end) {
        StringBuilder sb = new StringBuilder(original);
        return sb.delete(start, end).toString();
    }

    public static int getStringBuilderCapacity(int initialCapacity) {
        return new StringBuilder(initialCapacity).capacity();
    }
}