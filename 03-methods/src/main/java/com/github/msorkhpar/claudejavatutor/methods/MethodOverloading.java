package com.github.msorkhpar.claudejavatutor.methods;

public class MethodOverloading {

    // Overloaded methods with different number of parameters
    public int add(int a, int b) {
        return a + b;
    }

    public int add(int a, int b, int c) {
        return a + b + c;
    }

    // Overloaded methods with different types of parameters
    public double add(double a, double b) {
        return a + b;
    }

    public String add(String a, String b) {
        return a + b;
    }

    // Overloaded method with different order of parameters
    public String concat(String str, int num) {
        return str + num;
    }

    public String concat(int num, String str) {
        return num + str;
    }

    // Example of potential ambiguity (resolved by most specific type)
    public void print(Object obj) {
        System.out.println("Printing object: " + obj);
    }

    public void print(String str) {
        System.out.println("Printing string: " + str);
    }

    // Example with varargs
    public int sum(int... numbers) {
        int total = 0;
        for (int num : numbers) {
            total += num;
        }
        return total;
    }
}