package com.github.msorkhpar.claudejavatutor.datastructures;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Big O Notation Tests")
class BigONotationTest {

    // ========== Constant Time O(1) Tests ==========

    @Nested
    @DisplayName("O(1) Constant Time")
    class ConstantTimeTest {

        private final BigONotation.ConstantTime constant = new BigONotation.ConstantTime();

        @Test
        @DisplayName("Should get element by index in O(1)")
        void testGetElement() {
            int[] array = {10, 20, 30, 40, 50};
            assertThat(constant.getElement(array, 0)).isEqualTo(10);
            assertThat(constant.getElement(array, 4)).isEqualTo(50);
        }

        @Test
        @DisplayName("Should throw on null array")
        void testGetElementNullArray() {
            assertThatThrownBy(() -> constant.getElement(null, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw on invalid index")
        void testGetElementInvalidIndex() {
            int[] array = {1, 2, 3};
            assertThatThrownBy(() -> constant.getElement(array, -1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> constant.getElement(array, 3))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should get value from map in O(1) average")
        void testGetFromMap() {
            Map<String, String> map = Map.of("key1", "value1", "key2", "value2");
            assertThat(constant.getFromMap(map, "key1")).isEqualTo("value1");
            assertThat(constant.getFromMap(map, "missing")).isNull();
        }

        @Test
        @DisplayName("Should push and pop from stack in O(1)")
        void testPushAndPop() {
            Deque<Integer> stack = new ArrayDeque<>();
            assertThat(constant.pushAndPop(stack, 42)).isEqualTo(42);
            assertThat(stack).isEmpty();
        }

        @Test
        @DisplayName("Should check even/odd in O(1)")
        void testIsEven() {
            assertThat(constant.isEven(0)).isTrue();
            assertThat(constant.isEven(2)).isTrue();
            assertThat(constant.isEven(3)).isFalse();
            assertThat(constant.isEven(-4)).isTrue();
            assertThat(constant.isEven(-3)).isFalse();
        }
    }

    // ========== Logarithmic Time O(log n) Tests ==========

    @Nested
    @DisplayName("O(log n) Logarithmic Time")
    class LogarithmicTimeTest {

        private final BigONotation.LogarithmicTime logarithmic = new BigONotation.LogarithmicTime();

        @Test
        @DisplayName("Should find element via binary search")
        void testBinarySearch() {
            int[] sorted = {1, 3, 5, 7, 9, 11, 13, 15};
            assertThat(logarithmic.binarySearch(sorted, 7)).isEqualTo(3);
            assertThat(logarithmic.binarySearch(sorted, 1)).isEqualTo(0);
            assertThat(logarithmic.binarySearch(sorted, 15)).isEqualTo(7);
        }

        @Test
        @DisplayName("Should return -1 for missing element in binary search")
        void testBinarySearchNotFound() {
            int[] sorted = {1, 3, 5, 7, 9};
            assertThat(logarithmic.binarySearch(sorted, 4)).isEqualTo(-1);
            assertThat(logarithmic.binarySearch(sorted, 0)).isEqualTo(-1);
            assertThat(logarithmic.binarySearch(sorted, 10)).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should handle null and empty array in binary search")
        void testBinarySearchEdgeCases() {
            assertThat(logarithmic.binarySearch(null, 5)).isEqualTo(-1);
            assertThat(logarithmic.binarySearch(new int[]{}, 5)).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should handle single element array in binary search")
        void testBinarySearchSingleElement() {
            assertThat(logarithmic.binarySearch(new int[]{5}, 5)).isEqualTo(0);
            assertThat(logarithmic.binarySearch(new int[]{5}, 3)).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should compute fast power correctly")
        void testFastPower() {
            assertThat(logarithmic.fastPower(2, 10)).isEqualTo(1024);
            assertThat(logarithmic.fastPower(3, 0)).isEqualTo(1);
            assertThat(logarithmic.fastPower(5, 1)).isEqualTo(5);
        }

        @Test
        @DisplayName("Should throw on negative exponent")
        void testFastPowerNegativeExponent() {
            assertThatThrownBy(() -> logarithmic.fastPower(2, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should count digits correctly")
        void testCountDigits() {
            assertThat(logarithmic.countDigits(0)).isEqualTo(1);
            assertThat(logarithmic.countDigits(5)).isEqualTo(1);
            assertThat(logarithmic.countDigits(99)).isEqualTo(2);
            assertThat(logarithmic.countDigits(100)).isEqualTo(3);
            assertThat(logarithmic.countDigits(12345)).isEqualTo(5);
            assertThat(logarithmic.countDigits(-999)).isEqualTo(3);
        }
    }

    // ========== Linear Time O(n) Tests ==========

    @Nested
    @DisplayName("O(n) Linear Time")
    class LinearTimeTest {

        private final BigONotation.LinearTime linear = new BigONotation.LinearTime();

        @Test
        @DisplayName("Should find maximum element")
        void testFindMax() {
            assertThat(linear.findMax(new int[]{3, 1, 4, 1, 5, 9, 2, 6})).isEqualTo(9);
        }

        @Test
        @DisplayName("Should find max with single element")
        void testFindMaxSingle() {
            assertThat(linear.findMax(new int[]{42})).isEqualTo(42);
        }

        @Test
        @DisplayName("Should find max with negative numbers")
        void testFindMaxNegative() {
            assertThat(linear.findMax(new int[]{-5, -3, -8, -1})).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should throw on null or empty array for findMax")
        void testFindMaxInvalid() {
            assertThatThrownBy(() -> linear.findMax(null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> linear.findMax(new int[]{}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should perform linear search")
        void testLinearSearch() {
            int[] array = {10, 20, 30, 40, 50};
            assertThat(linear.linearSearch(array, 30)).isEqualTo(2);
            assertThat(linear.linearSearch(array, 10)).isEqualTo(0);
            assertThat(linear.linearSearch(array, 50)).isEqualTo(4);
            assertThat(linear.linearSearch(array, 99)).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should handle null array in linear search")
        void testLinearSearchNull() {
            assertThat(linear.linearSearch(null, 5)).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should sum all elements")
        void testSum() {
            assertThat(linear.sum(new int[]{1, 2, 3, 4, 5})).isEqualTo(15);
            assertThat(linear.sum(new int[]{})).isEqualTo(0);
            assertThat(linear.sum(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle integer overflow in sum using long")
        void testSumOverflow() {
            assertThat(linear.sum(new int[]{Integer.MAX_VALUE, 1})).isEqualTo((long) Integer.MAX_VALUE + 1);
        }

        @Test
        @DisplayName("Should reverse an array")
        void testReverse() {
            assertThat(linear.reverse(new int[]{1, 2, 3, 4, 5})).containsExactly(5, 4, 3, 2, 1);
            assertThat(linear.reverse(new int[]{1})).containsExactly(1);
            assertThat(linear.reverse(new int[]{})).isEmpty();
            assertThat(linear.reverse(null)).isNull();
        }

        @Test
        @DisplayName("Should check palindrome")
        void testIsPalindrome() {
            assertThat(linear.isPalindrome("racecar")).isTrue();
            assertThat(linear.isPalindrome("A man, a plan, a canal: Panama")).isTrue();
            assertThat(linear.isPalindrome("hello")).isFalse();
            assertThat(linear.isPalindrome("")).isTrue();
            assertThat(linear.isPalindrome("a")).isTrue();
            assertThat(linear.isPalindrome(null)).isFalse();
        }
    }

    // ========== Linearithmic Time O(n log n) Tests ==========

    @Nested
    @DisplayName("O(n log n) Linearithmic Time")
    class LinearithmicTimeTest {

        private final BigONotation.LinearithmicTime linearithmic = new BigONotation.LinearithmicTime();

        @Test
        @DisplayName("Should sort array using merge sort")
        void testMergeSort() {
            assertThat(linearithmic.mergeSort(new int[]{5, 2, 8, 1, 9, 3}))
                    .containsExactly(1, 2, 3, 5, 8, 9);
        }

        @Test
        @DisplayName("Should handle already sorted array")
        void testMergeSortAlreadySorted() {
            assertThat(linearithmic.mergeSort(new int[]{1, 2, 3, 4, 5}))
                    .containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should handle reverse sorted array")
        void testMergeSortReverseSorted() {
            assertThat(linearithmic.mergeSort(new int[]{5, 4, 3, 2, 1}))
                    .containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should handle edge cases in merge sort")
        void testMergeSortEdgeCases() {
            assertThat(linearithmic.mergeSort(null)).isNull();
            assertThat(linearithmic.mergeSort(new int[]{})).isEmpty();
            assertThat(linearithmic.mergeSort(new int[]{42})).containsExactly(42);
        }

        @Test
        @DisplayName("Should handle duplicates in merge sort")
        void testMergeSortWithDuplicates() {
            assertThat(linearithmic.mergeSort(new int[]{3, 1, 2, 3, 1}))
                    .containsExactly(1, 1, 2, 3, 3);
        }

        @Test
        @DisplayName("Should sort list using Collections.sort")
        void testSortWithCollections() {
            List<Integer> sorted = linearithmic.sortWithCollections(List.of(5, 3, 1, 4, 2));
            assertThat(sorted).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should handle null list in Collections sort")
        void testSortWithCollectionsNull() {
            assertThat(linearithmic.sortWithCollections(null)).isNull();
        }
    }

    // ========== Quadratic Time O(n^2) Tests ==========

    @Nested
    @DisplayName("O(n^2) Quadratic Time")
    class QuadraticTimeTest {

        private final BigONotation.QuadraticTime quadratic = new BigONotation.QuadraticTime();

        @Test
        @DisplayName("Should sort with bubble sort")
        void testBubbleSort() {
            assertThat(quadratic.bubbleSort(new int[]{5, 2, 8, 1, 9}))
                    .containsExactly(1, 2, 5, 8, 9);
        }

        @Test
        @DisplayName("Should handle edge cases in bubble sort")
        void testBubbleSortEdgeCases() {
            assertThat(quadratic.bubbleSort(null)).isNull();
            assertThat(quadratic.bubbleSort(new int[]{})).isEmpty();
            assertThat(quadratic.bubbleSort(new int[]{1})).containsExactly(1);
        }

        @Test
        @DisplayName("Should optimize bubble sort for already sorted array")
        void testBubbleSortAlreadySorted() {
            assertThat(quadratic.bubbleSort(new int[]{1, 2, 3, 4, 5}))
                    .containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should detect duplicates using naive O(n^2) approach")
        void testHasDuplicatesNaive() {
            assertThat(quadratic.hasDuplicatesNaive(new int[]{1, 2, 3, 4, 2})).isTrue();
            assertThat(quadratic.hasDuplicatesNaive(new int[]{1, 2, 3, 4, 5})).isFalse();
            assertThat(quadratic.hasDuplicatesNaive(new int[]{})).isFalse();
            assertThat(quadratic.hasDuplicatesNaive(null)).isFalse();
        }

        @Test
        @DisplayName("Should detect duplicates using optimized O(n) approach")
        void testHasDuplicatesOptimized() {
            assertThat(quadratic.hasDuplicatesOptimized(new int[]{1, 2, 3, 4, 2})).isTrue();
            assertThat(quadratic.hasDuplicatesOptimized(new int[]{1, 2, 3, 4, 5})).isFalse();
            assertThat(quadratic.hasDuplicatesOptimized(new int[]{})).isFalse();
            assertThat(quadratic.hasDuplicatesOptimized(null)).isFalse();
        }

        @Test
        @DisplayName("Both duplicate detection methods should agree")
        void testDuplicateDetectionConsistency() {
            int[][] testCases = {
                    {1, 2, 3, 4, 5},
                    {1, 1},
                    {5, 4, 3, 2, 1, 5},
                    {},
                    {42}
            };
            for (int[] testCase : testCases) {
                assertThat(quadratic.hasDuplicatesNaive(testCase))
                        .isEqualTo(quadratic.hasDuplicatesOptimized(testCase));
            }
        }

        @Test
        @DisplayName("Should find pairs that sum to target (naive)")
        void testFindPairsNaive() {
            List<int[]> pairs = quadratic.findPairsNaive(new int[]{1, 2, 3, 4, 5}, 6);
            assertThat(pairs).hasSize(2);
            assertThat(pairs.get(0)).containsExactly(1, 5);
            assertThat(pairs.get(1)).containsExactly(2, 4);
        }

        @Test
        @DisplayName("Should find pairs that sum to target (optimized)")
        void testFindPairsOptimized() {
            List<int[]> pairs = quadratic.findPairsOptimized(new int[]{1, 2, 3, 4, 5}, 6);
            assertThat(pairs).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty pairs for null array")
        void testFindPairsNull() {
            assertThat(quadratic.findPairsNaive(null, 5)).isEmpty();
            assertThat(quadratic.findPairsOptimized(null, 5)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty pairs when no pair sums to target")
        void testFindPairsNoPairs() {
            assertThat(quadratic.findPairsNaive(new int[]{1, 2, 3}, 100)).isEmpty();
            assertThat(quadratic.findPairsOptimized(new int[]{1, 2, 3}, 100)).isEmpty();
        }
    }

    // ========== Space Complexity Tests ==========

    @Nested
    @DisplayName("Space Complexity")
    class SpaceComplexityTest {

        private final BigONotation.SpaceComplexity space = new BigONotation.SpaceComplexity();

        @Test
        @DisplayName("Should swap elements in place O(1) space")
        void testSwapInPlace() {
            int[] array = {1, 2, 3, 4, 5};
            space.swapInPlace(array, 0, 4);
            assertThat(array).containsExactly(5, 2, 3, 4, 1);
        }

        @Test
        @DisplayName("Should throw on invalid indices for swap")
        void testSwapInPlaceInvalid() {
            assertThatThrownBy(() -> space.swapInPlace(null, 0, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> space.swapInPlace(new int[]{1}, -1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> space.swapInPlace(new int[]{1}, 0, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should compute fibonacci iteratively O(1) space")
        void testFibonacciIterative() {
            assertThat(space.fibonacciIterative(0)).isEqualTo(0);
            assertThat(space.fibonacciIterative(1)).isEqualTo(1);
            assertThat(space.fibonacciIterative(2)).isEqualTo(1);
            assertThat(space.fibonacciIterative(10)).isEqualTo(55);
            assertThat(space.fibonacciIterative(20)).isEqualTo(6765);
        }

        @Test
        @DisplayName("Should throw on negative fibonacci input")
        void testFibonacciNegative() {
            assertThatThrownBy(() -> space.fibonacciIterative(-1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> space.fibonacciRecursive(-1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> space.fibonacciMemoized(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should compute fibonacci recursively")
        void testFibonacciRecursive() {
            assertThat(space.fibonacciRecursive(0)).isEqualTo(0);
            assertThat(space.fibonacciRecursive(1)).isEqualTo(1);
            assertThat(space.fibonacciRecursive(10)).isEqualTo(55);
        }

        @Test
        @DisplayName("Should compute fibonacci with memoization")
        void testFibonacciMemoized() {
            assertThat(space.fibonacciMemoized(0)).isEqualTo(0);
            assertThat(space.fibonacciMemoized(1)).isEqualTo(1);
            assertThat(space.fibonacciMemoized(10)).isEqualTo(55);
            assertThat(space.fibonacciMemoized(30)).isEqualTo(832040);
        }

        @Test
        @DisplayName("All fibonacci methods should agree for small values")
        void testFibonacciConsistency() {
            for (int i = 0; i <= 15; i++) {
                long iterative = space.fibonacciIterative(i);
                long recursive = space.fibonacciRecursive(i);
                long memoized = space.fibonacciMemoized(i);
                assertThat(iterative).isEqualTo(recursive).isEqualTo(memoized);
            }
        }

        @Test
        @DisplayName("Should compute character frequency O(n) space")
        void testCharacterFrequency() {
            Map<Character, Integer> freq = space.characterFrequency("hello");
            assertThat(freq).containsEntry('h', 1)
                    .containsEntry('e', 1)
                    .containsEntry('l', 2)
                    .containsEntry('o', 1);
        }

        @Test
        @DisplayName("Should handle null and empty string for character frequency")
        void testCharacterFrequencyEdgeCases() {
            assertThat(space.characterFrequency(null)).isEmpty();
            assertThat(space.characterFrequency("")).isEmpty();
        }

        @Test
        @DisplayName("Should create matrix O(n^2) space")
        void testCreateMatrix() {
            int[][] matrix = space.createMatrix(3);
            assertThat(matrix).hasNumberOfRows(3);
            assertThat(matrix[0]).containsExactly(0, 1, 2);
            assertThat(matrix[1]).containsExactly(3, 4, 5);
            assertThat(matrix[2]).containsExactly(6, 7, 8);
        }

        @Test
        @DisplayName("Should handle zero-size matrix")
        void testCreateMatrixZero() {
            int[][] matrix = space.createMatrix(0);
            assertThat(matrix).isEmpty();
        }

        @Test
        @DisplayName("Should throw on negative matrix size")
        void testCreateMatrixNegative() {
            assertThatThrownBy(() -> space.createMatrix(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ========== Case Analysis Tests ==========

    @Nested
    @DisplayName("Best/Average/Worst Case Analysis")
    class CaseAnalysisTest {

        private final BigONotation.CaseAnalysis analysis = new BigONotation.CaseAnalysis();

        @Test
        @DisplayName("Should sort with quicksort")
        void testQuickSort() {
            assertThat(analysis.quickSort(new int[]{5, 2, 8, 1, 9, 3}))
                    .containsExactly(1, 2, 3, 5, 8, 9);
        }

        @Test
        @DisplayName("Should handle edge cases in quicksort")
        void testQuickSortEdgeCases() {
            assertThat(analysis.quickSort(null)).isNull();
            assertThat(analysis.quickSort(new int[]{})).isEmpty();
            assertThat(analysis.quickSort(new int[]{1})).containsExactly(1);
        }

        @Test
        @DisplayName("Should handle already sorted array in quicksort")
        void testQuickSortAlreadySorted() {
            assertThat(analysis.quickSort(new int[]{1, 2, 3, 4, 5}))
                    .containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should handle reverse sorted array in quicksort")
        void testQuickSortReverseSorted() {
            assertThat(analysis.quickSort(new int[]{5, 4, 3, 2, 1}))
                    .containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should handle duplicates in quicksort")
        void testQuickSortDuplicates() {
            assertThat(analysis.quickSort(new int[]{3, 1, 2, 3, 1, 2}))
                    .containsExactly(1, 1, 2, 2, 3, 3);
        }

        @Test
        @DisplayName("Should count comparisons in linear search (best case: 1)")
        void testLinearSearchBestCase() {
            int[] array = {42, 1, 2, 3, 4};
            int comparisons = analysis.linearSearchWithCount(array, 42);
            assertThat(comparisons).isEqualTo(1); // Found at first position
        }

        @Test
        @DisplayName("Should count comparisons in linear search (worst case: n)")
        void testLinearSearchWorstCase() {
            int[] array = {1, 2, 3, 4, 42};
            int comparisons = analysis.linearSearchWithCount(array, 42);
            assertThat(comparisons).isEqualTo(5); // Found at last position
        }

        @Test
        @DisplayName("Should count comparisons when element not found")
        void testLinearSearchNotFound() {
            int[] array = {1, 2, 3};
            int comparisons = analysis.linearSearchWithCount(array, 99);
            assertThat(comparisons).isEqualTo(3); // Checked all elements
        }

        @Test
        @DisplayName("Should handle null in linear search with count")
        void testLinearSearchWithCountNull() {
            assertThat(analysis.linearSearchWithCount(null, 5)).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should get from HashMap")
        void testHashMapGet() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2);
            assertThat(analysis.hashMapGet(map, "a")).isEqualTo(1);
            assertThat(analysis.hashMapGet(map, "missing")).isNull();
        }

        @Test
        @DisplayName("Should sort with insertion sort")
        void testInsertionSort() {
            assertThat(analysis.insertionSort(new int[]{5, 2, 8, 1, 9}))
                    .containsExactly(1, 2, 5, 8, 9);
        }

        @Test
        @DisplayName("Should handle edge cases in insertion sort")
        void testInsertionSortEdgeCases() {
            assertThat(analysis.insertionSort(null)).isNull();
            assertThat(analysis.insertionSort(new int[]{})).isEmpty();
            assertThat(analysis.insertionSort(new int[]{1})).containsExactly(1);
        }

        @Test
        @DisplayName("Insertion sort should handle already sorted array efficiently")
        void testInsertionSortBestCase() {
            assertThat(analysis.insertionSort(new int[]{1, 2, 3, 4, 5}))
                    .containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Insertion sort should handle reverse sorted array")
        void testInsertionSortWorstCase() {
            assertThat(analysis.insertionSort(new int[]{5, 4, 3, 2, 1}))
                    .containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Original array should not be modified by sorting methods")
        void testSortingDoesNotModifyOriginal() {
            int[] original = {5, 2, 8, 1, 9};
            int[] copy = original.clone();

            analysis.quickSort(original);
            assertThat(original).containsExactly(copy);

            analysis.insertionSort(original);
            assertThat(original).containsExactly(copy);
        }
    }
}
