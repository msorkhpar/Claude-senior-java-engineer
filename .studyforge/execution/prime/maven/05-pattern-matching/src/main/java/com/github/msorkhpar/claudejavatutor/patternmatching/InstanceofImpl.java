package com.github.msorkhpar.claudejavatutor.patternmatching;

public class InstanceofImpl {

    public static String checkType(Object obj) {
        if (obj instanceof String) {
            return "String";
        } else if (obj instanceof Integer) {
            return "Integer";
        } else if (obj instanceof Double) {
            return "Double";
        } else if (obj instanceof Object[]) {
            return "Object Array";
        } else if (obj == null) {
            return "Null";
        } else {
            return "Other";
        }
    }

    public static boolean isNumber(Object obj) {
        return obj instanceof Number;
    }

    public static boolean isStringOrNumber(Object obj) {
        return obj instanceof String || obj instanceof Number;
    }
}