package com.github.msorkhpar.claudejavatutor.controlflow;

public class Loop {

    /**
     * Calculates the sum of numbers from 1 to n using a while loop.
     *
     * @param n The upper limit of the sum.
     * @return The sum of numbers from 1 to n.
     */
    public static int sumUsingWhile(int n) {
        int sum = 0;
        int i = 1;
        while (i <= n) {
            sum += i;
            i++;
        }
        return sum;
    }

    /**
     * Calculates the factorial of a number using a do-while loop.
     *
     * @param n The number to calculate factorial for.
     * @return The factorial of n.
     * @throws IllegalArgumentException if n is negative.
     */
    public static long factorialUsingDoWhile(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Factorial is not defined for negative numbers");
        }
        long factorial = 1;
        int i = 1;
        do {
            factorial *= i;
            i++;
        } while (i <= n);
        return factorial;
    }

    /**
     * Finds the first occurrence of a target value in an array using a while loop.
     *
     * @param arr    The array to search.
     * @param target The value to find.
     * @return The index of the first occurrence of the target, or -1 if not found.
     */
    public static int findFirstOccurrence(int[] arr, int target) {
        int index = 0;
        while (index < arr.length) {
            if (arr[index] == target) {
                return index;
            }
            index++;
        }
        return -1;
    }
}