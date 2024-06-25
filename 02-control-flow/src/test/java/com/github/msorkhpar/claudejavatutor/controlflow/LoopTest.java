package com.github.msorkhpar.claudejavatutor.controlflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class LoopTest {

    @ParameterizedTest
    @CsvSource({"0,0", "1,1", "5,15", "10,55"})
    void testSumUsingWhile(int input, int expected) {
        assertEquals(expected, Loop.sumUsingWhile(input));
    }

    @ParameterizedTest
    @CsvSource({"0,1", "1,1", "5,120", "10,3628800"})
    void testFactorialUsingDoWhile(int input, long expected) {
        assertEquals(expected, Loop.factorialUsingDoWhile(input));
    }

    @Test
    void testFactorialUsingDoWhileWithNegativeInput() {
        assertThrows(IllegalArgumentException.class, () -> Loop.factorialUsingDoWhile(-1));
    }

    @Test
    void testFindFirstOccurrence() {
        int[] arr = {1, 3, 5, 7, 9, 3, 2};
        assertEquals(1, Loop.findFirstOccurrence(arr, 3));
        assertEquals(0, Loop.findFirstOccurrence(arr, 1));
        assertEquals(6, Loop.findFirstOccurrence(arr, 2));
        assertEquals(-1, Loop.findFirstOccurrence(arr, 4));
    }

    @Test
    void testFindFirstOccurrenceEmptyArray() {
        int[] emptyArr = {};
        assertEquals(-1, Loop.findFirstOccurrence(emptyArr, 5));
    }
}