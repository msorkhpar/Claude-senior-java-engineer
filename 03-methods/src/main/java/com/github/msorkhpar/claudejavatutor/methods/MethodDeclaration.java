package com.github.msorkhpar.claudejavatutor.methods;

import java.util.List;

public class MethodDeclaration {

    // Instance method with no parameters and void return type
    public void printHello() {
        System.out.println("Hello, World!");
    }

    // Static method with parameters and return value
    public static int add(int a, int b) {
        return a + b;
    }

    // Method with multiple parameters and exception handling
    public double divide(double numerator, double denominator) throws ArithmeticException {
        if (denominator == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return numerator / denominator;
    }

    // Method demonstrating parameter validation
    public void processList(List<String> items) {
        if (items == null || items.isEmpty()) {
            System.out.println("The list is null or empty");
            return;
        }
        for (String item : items) {
            System.out.println("Processing: " + item);
        }
    }

    // Method with variable number of arguments (varargs)
    public int sum(int... numbers) {
        int total = 0;
        for (int num : numbers) {
            total += num;
        }
        return total;
    }
}