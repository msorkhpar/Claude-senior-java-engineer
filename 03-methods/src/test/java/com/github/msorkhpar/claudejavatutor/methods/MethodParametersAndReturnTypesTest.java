package com.github.msorkhpar.claudejavatutor.methods;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class MethodParametersAndReturnTypesTest {

    @ParameterizedTest
    @CsvSource({"1, 2, 3", "0, 0, 0", "-1, 1, 0", "10, -5, 5"})
    @DisplayName("add method should correctly sum two integers")
    void testAdd(int a, int b, int expected) {
        assertEquals(expected, MethodParametersAndReturnTypes.add(a, b));
    }

    @Test
    @DisplayName("modifyList should add 'Modified' to the list")
    void testModifyList() {
        List<String> list = new ArrayList<>(Arrays.asList("Original"));
        MethodParametersAndReturnTypes.modifyList(list);
        assertThat(list).containsExactly("Original", "Modified");
    }

    @Test
    @DisplayName("modifyList should handle null input")
    void testModifyListWithNull() {
        assertDoesNotThrow(() -> MethodParametersAndReturnTypes.modifyList(null));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10})
    @DisplayName("calculateFactorial should return correct result for non-negative inputs")
    void testCalculateFactorial(int input) {
        long expected = 1;
        for (int i = 2; i <= input; i++) {
            expected *= i;
        }
        assertEquals(expected, MethodParametersAndReturnTypes.calculateFactorial(input));
    }

    @Test
    @DisplayName("calculateFactorial should throw IllegalArgumentException for negative input")
    void testCalculateFactorialNegativeInput() {
        assertThatThrownBy(() -> MethodParametersAndReturnTypes.calculateFactorial(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Factorial is not defined for negative numbers");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5})
    @DisplayName("generateEvenNumbers should return correct list of even numbers")
    void testGenerateEvenNumbers(int count) {
        List<Integer> result = MethodParametersAndReturnTypes.generateEvenNumbers(count);
        assertThat(result).hasSize(count);
        assertThat(result).allMatch(n -> n % 2 == 0);
        for (int i = 0; i < count; i++) {
            assertEquals(i * 2, result.get(i));
        }
    }

    @Test
    @DisplayName("generateEvenNumbers should return empty list for negative count")
    void testGenerateEvenNumbersNegativeCount() {
        List<Integer> result = MethodParametersAndReturnTypes.generateEvenNumbers(-1);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findLongestString should return the longest string")
    void testFindLongestString() {
        List<String> strings = Arrays.asList("a", "bb", "ccc", "d");
        Optional<String> result = MethodParametersAndReturnTypes.findLongestString(strings);
        assertThat(result).isPresent().contains("ccc");
    }

    @Test
    @DisplayName("findLongestString should return empty Optional for empty list")
    void testFindLongestStringEmptyList() {
        List<String> strings = new ArrayList<>();
        Optional<String> result = MethodParametersAndReturnTypes.findLongestString(strings);
        assertThat(result).isEmpty();
    }
}