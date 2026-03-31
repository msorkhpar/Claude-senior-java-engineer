package com.github.msorkhpar.claudejavatutor.datastructures;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Demonstrates Big O Notation concepts through practical Java examples.
 * Covers time complexity, space complexity, and best/average/worst case analysis.
 */
public class BigONotation {

    // ========== 10.1.2.1 Time Complexity Examples ==========

    /**
     * O(1) - Constant time operations.
     * The operation takes the same amount of time regardless of input size.
     */
    public static class ConstantTime {

        /**
         * O(1) - Array access by index.
         */
        public int getElement(int[] array, int index) {
            if (array == null || index < 0 || index >= array.length) {
                throw new IllegalArgumentException("Invalid array or index");
            }
            return array[index];
        }

        /**
         * O(1) - HashMap get operation (average case).
         */
        public String getFromMap(Map<String, String> map, String key) {
            return map.get(key);
        }

        /**
         * O(1) - Stack push/pop operations.
         */
        public int pushAndPop(Deque<Integer> stack, int value) {
            stack.push(value);
            return stack.pop();
        }

        /**
         * O(1) - Check if a number is even.
         */
        public boolean isEven(int number) {
            return (number & 1) == 0;
        }
    }

    /**
     * O(log n) - Logarithmic time operations.
     * The operation time grows logarithmically with input size.
     * Typically found in divide-and-conquer algorithms.
     */
    public static class LogarithmicTime {

        /**
         * O(log n) - Binary search on a sorted array.
         */
        public int binarySearch(int[] sortedArray, int target) {
            if (sortedArray == null || sortedArray.length == 0) {
                return -1;
            }
            int left = 0;
            int right = sortedArray.length - 1;
            while (left <= right) {
                int mid = left + (right - left) / 2; // Avoid integer overflow
                if (sortedArray[mid] == target) {
                    return mid;
                } else if (sortedArray[mid] < target) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
            return -1;
        }

        /**
         * O(log n) - Finding power using fast exponentiation.
         */
        public long fastPower(long base, long exponent) {
            if (exponent < 0) {
                throw new IllegalArgumentException("Exponent must be non-negative");
            }
            long result = 1;
            base = base % 1_000_000_007; // Prevent overflow
            while (exponent > 0) {
                if ((exponent & 1) == 1) {
                    result = (result * base) % 1_000_000_007;
                }
                exponent >>= 1;
                base = (base * base) % 1_000_000_007;
            }
            return result;
        }

        /**
         * O(log n) - Count the number of digits in a number.
         */
        public int countDigits(long number) {
            if (number == 0) return 1;
            number = Math.abs(number);
            return (int) Math.floor(Math.log10(number)) + 1;
        }
    }

    /**
     * O(n) - Linear time operations.
     * The operation time grows linearly with input size.
     */
    public static class LinearTime {

        /**
         * O(n) - Find maximum element in an unsorted array.
         */
        public int findMax(int[] array) {
            if (array == null || array.length == 0) {
                throw new IllegalArgumentException("Array must not be null or empty");
            }
            int max = array[0];
            for (int i = 1; i < array.length; i++) {
                if (array[i] > max) {
                    max = array[i];
                }
            }
            return max;
        }

        /**
         * O(n) - Linear search.
         */
        public int linearSearch(int[] array, int target) {
            if (array == null) return -1;
            for (int i = 0; i < array.length; i++) {
                if (array[i] == target) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * O(n) - Sum all elements.
         */
        public long sum(int[] array) {
            if (array == null) return 0;
            long total = 0;
            for (int value : array) {
                total += value;
            }
            return total;
        }

        /**
         * O(n) - Reverse an array in place.
         */
        public int[] reverse(int[] array) {
            if (array == null) return null;
            int[] result = array.clone();
            int left = 0, right = result.length - 1;
            while (left < right) {
                int temp = result[left];
                result[left] = result[right];
                result[right] = temp;
                left++;
                right--;
            }
            return result;
        }

        /**
         * O(n) - Two-pointer technique to check if a string is a palindrome.
         */
        public boolean isPalindrome(String s) {
            if (s == null) return false;
            String cleaned = s.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            int left = 0, right = cleaned.length() - 1;
            while (left < right) {
                if (cleaned.charAt(left) != cleaned.charAt(right)) {
                    return false;
                }
                left++;
                right--;
            }
            return true;
        }
    }

    /**
     * O(n log n) - Linearithmic time operations.
     * Common in efficient sorting algorithms.
     */
    public static class LinearithmicTime {

        /**
         * O(n log n) - Merge sort implementation.
         */
        public int[] mergeSort(int[] array) {
            if (array == null || array.length <= 1) {
                return array == null ? null : array.clone();
            }
            int[] result = array.clone();
            mergeSortRec(result, 0, result.length - 1);
            return result;
        }

        private void mergeSortRec(int[] array, int left, int right) {
            if (left < right) {
                int mid = left + (right - left) / 2;
                mergeSortRec(array, left, mid);
                mergeSortRec(array, mid + 1, right);
                merge(array, left, mid, right);
            }
        }

        private void merge(int[] array, int left, int mid, int right) {
            int[] temp = new int[right - left + 1];
            int i = left, j = mid + 1, k = 0;
            while (i <= mid && j <= right) {
                if (array[i] <= array[j]) {
                    temp[k++] = array[i++];
                } else {
                    temp[k++] = array[j++];
                }
            }
            while (i <= mid) temp[k++] = array[i++];
            while (j <= right) temp[k++] = array[j++];
            System.arraycopy(temp, 0, array, left, temp.length);
        }

        /**
         * O(n log n) - Sort using Java's built-in sort (TimSort).
         */
        public List<Integer> sortWithCollections(List<Integer> list) {
            if (list == null) return null;
            List<Integer> sorted = new ArrayList<>(list);
            Collections.sort(sorted);
            return sorted;
        }
    }

    /**
     * O(n^2) - Quadratic time operations.
     * Common in naive sorting algorithms and nested loops.
     */
    public static class QuadraticTime {

        /**
         * O(n^2) - Bubble sort implementation.
         */
        public int[] bubbleSort(int[] array) {
            if (array == null || array.length <= 1) {
                return array == null ? null : array.clone();
            }
            int[] result = array.clone();
            int n = result.length;
            for (int i = 0; i < n - 1; i++) {
                boolean swapped = false;
                for (int j = 0; j < n - 1 - i; j++) {
                    if (result[j] > result[j + 1]) {
                        int temp = result[j];
                        result[j] = result[j + 1];
                        result[j + 1] = temp;
                        swapped = true;
                    }
                }
                if (!swapped) break; // Optimization: stop if already sorted
            }
            return result;
        }

        /**
         * O(n^2) - Check for duplicates using nested loops (naive approach).
         */
        public boolean hasDuplicatesNaive(int[] array) {
            if (array == null) return false;
            for (int i = 0; i < array.length; i++) {
                for (int j = i + 1; j < array.length; j++) {
                    if (array[i] == array[j]) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * O(n) - Check for duplicates using HashSet (optimized approach).
         * Demonstrates how choosing the right data structure improves time complexity.
         */
        public boolean hasDuplicatesOptimized(int[] array) {
            if (array == null) return false;
            Set<Integer> seen = new HashSet<>();
            for (int value : array) {
                if (!seen.add(value)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * O(n^2) - Find all pairs that sum to a target (naive approach).
         */
        public List<int[]> findPairsNaive(int[] array, int target) {
            List<int[]> pairs = new ArrayList<>();
            if (array == null) return pairs;
            for (int i = 0; i < array.length; i++) {
                for (int j = i + 1; j < array.length; j++) {
                    if (array[i] + array[j] == target) {
                        pairs.add(new int[]{array[i], array[j]});
                    }
                }
            }
            return pairs;
        }

        /**
         * O(n) - Find all pairs that sum to a target using HashMap (optimized approach).
         */
        public List<int[]> findPairsOptimized(int[] array, int target) {
            List<int[]> pairs = new ArrayList<>();
            if (array == null) return pairs;
            Set<Integer> seen = new HashSet<>();
            Set<String> usedPairs = new HashSet<>();
            for (int value : array) {
                int complement = target - value;
                if (seen.contains(complement)) {
                    int min = Math.min(value, complement);
                    int max = Math.max(value, complement);
                    String key = min + "," + max;
                    if (usedPairs.add(key)) {
                        pairs.add(new int[]{min, max});
                    }
                }
                seen.add(value);
            }
            return pairs;
        }
    }

    // ========== 10.1.2.2 Space Complexity Examples ==========

    /**
     * Demonstrates space complexity through various examples.
     */
    public static class SpaceComplexity {

        /**
         * O(1) space - Swap two elements in-place.
         */
        public void swapInPlace(int[] array, int i, int j) {
            if (array == null || i < 0 || j < 0 || i >= array.length || j >= array.length) {
                throw new IllegalArgumentException("Invalid array or indices");
            }
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }

        /**
         * O(1) space - Iterative fibonacci (only stores previous two values).
         */
        public long fibonacciIterative(int n) {
            if (n < 0) throw new IllegalArgumentException("n must be non-negative");
            if (n <= 1) return n;
            long prev2 = 0, prev1 = 1;
            for (int i = 2; i <= n; i++) {
                long current = prev1 + prev2;
                prev2 = prev1;
                prev1 = current;
            }
            return prev1;
        }

        /**
         * O(n) space - Create a frequency map.
         */
        public Map<Character, Integer> characterFrequency(String s) {
            if (s == null) return Collections.emptyMap();
            Map<Character, Integer> freq = new HashMap<>();
            for (char c : s.toCharArray()) {
                freq.merge(c, 1, Integer::sum);
            }
            return freq;
        }

        /**
         * O(n) space - Recursive fibonacci (O(n) call stack depth).
         * Note: This is also O(2^n) time without memoization.
         */
        public long fibonacciRecursive(int n) {
            if (n < 0) throw new IllegalArgumentException("n must be non-negative");
            if (n <= 1) return n;
            return fibonacciRecursive(n - 1) + fibonacciRecursive(n - 2);
        }

        /**
         * O(n) space and O(n) time - Fibonacci with memoization.
         */
        public long fibonacciMemoized(int n) {
            if (n < 0) throw new IllegalArgumentException("n must be non-negative");
            Map<Integer, Long> memo = new HashMap<>();
            return fibMemo(n, memo);
        }

        private long fibMemo(int n, Map<Integer, Long> memo) {
            if (n <= 1) return n;
            if (memo.containsKey(n)) return memo.get(n);
            long result = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
            memo.put(n, result);
            return result;
        }

        /**
         * O(n^2) space - Create an n x n matrix.
         */
        public int[][] createMatrix(int n) {
            if (n < 0) throw new IllegalArgumentException("n must be non-negative");
            int[][] matrix = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    matrix[i][j] = i * n + j;
                }
            }
            return matrix;
        }
    }

    // ========== 10.1.2.3 Best, Average, Worst Case Analysis ==========

    /**
     * Demonstrates best/average/worst case through QuickSort and other algorithms.
     */
    public static class CaseAnalysis {

        /**
         * QuickSort implementation demonstrating different time complexity cases:
         * - Best case: O(n log n) when pivot divides array evenly
         * - Average case: O(n log n)
         * - Worst case: O(n^2) when array is already sorted and pivot is always min/max
         */
        public int[] quickSort(int[] array) {
            if (array == null || array.length <= 1) {
                return array == null ? null : array.clone();
            }
            int[] result = array.clone();
            quickSortRec(result, 0, result.length - 1);
            return result;
        }

        private void quickSortRec(int[] array, int low, int high) {
            if (low < high) {
                int pivotIndex = partition(array, low, high);
                quickSortRec(array, low, pivotIndex - 1);
                quickSortRec(array, pivotIndex + 1, high);
            }
        }

        private int partition(int[] array, int low, int high) {
            // Median-of-three pivot selection to reduce worst-case scenarios
            int mid = low + (high - low) / 2;
            if (array[low] > array[mid]) swap(array, low, mid);
            if (array[low] > array[high]) swap(array, low, high);
            if (array[mid] > array[high]) swap(array, mid, high);
            swap(array, mid, high); // Move pivot to end

            int pivot = array[high];
            int i = low - 1;
            for (int j = low; j < high; j++) {
                if (array[j] <= pivot) {
                    i++;
                    swap(array, i, j);
                }
            }
            swap(array, i + 1, high);
            return i + 1;
        }

        private void swap(int[] array, int i, int j) {
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }

        /**
         * Linear search demonstrating different cases:
         * - Best case: O(1) when element is at the first position
         * - Average case: O(n/2) = O(n)
         * - Worst case: O(n) when element is at the last position or not found
         */
        public int linearSearchWithCount(int[] array, int target) {
            if (array == null) return -1;
            int comparisons = 0;
            for (int i = 0; i < array.length; i++) {
                comparisons++;
                if (array[i] == target) {
                    return comparisons;
                }
            }
            return comparisons; // Returns number of comparisons made
        }

        /**
         * HashMap get operation demonstrating different cases:
         * - Best/Average case: O(1) with good hash distribution
         * - Worst case: O(n) when all keys hash to same bucket (pre-Java 8)
         *   or O(log n) with treeification (Java 8+)
         */
        public <K, V> V hashMapGet(Map<K, V> map, K key) {
            return map.get(key);
        }

        /**
         * Insertion sort - demonstrates an algorithm where best case differs significantly from worst:
         * - Best case: O(n) when array is already sorted
         * - Average case: O(n^2)
         * - Worst case: O(n^2) when array is reverse sorted
         */
        public int[] insertionSort(int[] array) {
            if (array == null || array.length <= 1) {
                return array == null ? null : array.clone();
            }
            int[] result = array.clone();
            for (int i = 1; i < result.length; i++) {
                int key = result[i];
                int j = i - 1;
                while (j >= 0 && result[j] > key) {
                    result[j + 1] = result[j];
                    j--;
                }
                result[j + 1] = key;
            }
            return result;
        }
    }
}
