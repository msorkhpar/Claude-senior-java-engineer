package com.github.msorkhpar.claudejavatutor.controlflow;

import java.util.List;

public class ForLoop {

    public static int sumOfNumbers(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += i;
        }
        return sum;
    }

    public static String reverseString(String input) {
        StringBuilder reversed = new StringBuilder();
        for (int i = input.length() - 1; i >= 0; i--) {
            reversed.append(input.charAt(i));
        }
        return reversed.toString();
    }

    public static int[] filterEvenNumbers(int[] numbers) {
        int count = 0;
        for (int num : numbers) {
            if (num % 2 == 0) {
                count++;
            }
        }
        
        int[] evenNumbers = new int[count];
        int index = 0;
        for (int num : numbers) {
            if (num % 2 == 0) {
                evenNumbers[index++] = num;
            }
        }
        return evenNumbers;
    }

    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

    public static <T> void processListWithIndex(List<T> list, ListProcessor<T> processor) {
        for (int i = 0; i < list.size(); i++) {
            processor.process(i, list.get(i));
        }
    }

    @FunctionalInterface
    public interface ListProcessor<T> {
        void process(int index, T item);
    }
}