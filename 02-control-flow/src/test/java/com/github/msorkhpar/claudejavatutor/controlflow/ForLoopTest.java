package com.github.msorkhpar.claudejavatutor.controlflow;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

class ForLoopTest {

    @Test
    void testSumOfNumbers() {
        assertEquals(15, ForLoop.sumOfNumbers(5));
        assertEquals(0, ForLoop.sumOfNumbers(0));
        assertEquals(1, ForLoop.sumOfNumbers(1));
    }

    @Test
    void testReverseString() {
        assertEquals("olleH", ForLoop.reverseString("Hello"));
        assertEquals("", ForLoop.reverseString(""));
        assertEquals("a", ForLoop.reverseString("a"));
    }

    @Test
    void testFilterEvenNumbers() {
        assertArrayEquals(new int[]{2, 4, 6}, ForLoop.filterEvenNumbers(new int[]{1, 2, 3, 4, 5, 6}));
        assertArrayEquals(new int[]{}, ForLoop.filterEvenNumbers(new int[]{1, 3, 5}));
        assertArrayEquals(new int[]{2, 4}, ForLoop.filterEvenNumbers(new int[]{2, 4}));
    }

    @Test
    void testPrintMatrix() {
        int[][] matrix = {{1, 2}, {3, 4}};
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        ForLoop.printMatrix(matrix);

        String expectedOutput = "1 2 \n3 4 \n";
        assertEquals(expectedOutput, outContent.toString());

        System.setOut(System.out);
    }

    @Test
    void testProcessListWithIndex() {
        List<String> list = Arrays.asList("a", "b", "c");
        ForLoop.ListProcessor<String> mockProcessor = mock(ForLoop.ListProcessor.class);

        ForLoop.processListWithIndex(list, mockProcessor);

        verify(mockProcessor).process(0, "a");
        verify(mockProcessor).process(1, "b");
        verify(mockProcessor).process(2, "c");
        verifyNoMoreInteractions(mockProcessor);
    }
}