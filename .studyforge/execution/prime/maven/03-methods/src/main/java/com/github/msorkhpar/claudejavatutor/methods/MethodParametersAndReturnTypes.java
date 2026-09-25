package com.github.msorkhpar.claudejavatutor.methods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MethodParametersAndReturnTypes {

    // Method with multiple parameters
    public static int add(int a, int b) {
        return a + b;
    }

    // Method with an object parameter
    public static void modifyList(List<String> list) {
        if (list != null) {
            list.add("Modified");
        }
    }

    // Method returning void
    public static void printMessage(String message) {
        System.out.println(message);
    }

    // Method returning a primitive
    public static long calculateFactorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Factorial is not defined for negative numbers");
        }
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    // Method returning an object
    public static List<Integer> generateEvenNumbers(int count) {
        if (count < 0) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(i * 2);
        }
        return result;
    }

    // Method using Optional as return type
    public static Optional<String> findLongestString(List<String> strings) {
        return strings.stream()
                .max((s1, s2) -> Integer.compare(s1.length(), s2.length()));
    }
}
