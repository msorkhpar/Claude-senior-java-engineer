package com.github.msorkhpar.claudejavatutor.methods;

public class PassByValueReference {
    public static void modifyPrimitive(int value) {
        value = 20;
    }

    public static void modifyObject(StringBuilder sb) {
        sb.append(" World");
    }

    public static void reassignObject(StringBuilder sb) {
        sb = new StringBuilder("New Object");
    }

    public static void modifyArray(int[] arr) {
        arr[0] = 100;
    }

    public static void reassignArray(int[] arr) {
        arr = new int[]{200, 300};
    }

    public static void reassignReference(StringBuilder sb) {
        sb = new StringBuilder("New String");  // This reassignment is local to the method
    }
}