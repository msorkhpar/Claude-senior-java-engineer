package com.github.msorkhpar.claudejavatutor.methods;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class MethodDeclarationTest {

    private MethodDeclaration example;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        example = new MethodDeclaration();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void testPrintHello() {
        example.printHello();
        assertEquals("Hello, World!\n", outContent.toString());
    }

    @ParameterizedTest
    @CsvSource({"1, 2, 3", "0, 0, 0", "-1, 1, 0", "100, 200, 300"})
    void testAdd(int a, int b, int expected) {
        assertEquals(expected, MethodDeclaration.add(a, b));
    }

    @ParameterizedTest
    @CsvSource({"10, 2, 5", "15, 3, 5", "1, 3, 0.3333333333333333"})
    void testDivide(double numerator, double denominator, double expected) {
        assertEquals(expected, example.divide(numerator, denominator), 0.0000001);
    }

    @Test
    void testDivideByZero() {
        assertThrows(ArithmeticException.class, () -> example.divide(10, 0));
    }

    @Test
    void testProcessListWithValidInput() {
        example.processList(Arrays.asList("item1", "item2", "item3"));
        String expected = "Processing: item1\nProcessing: item2\nProcessing: item3\n";
        assertEquals(expected, outContent.toString());
    }

    @Test
    void testProcessListWithNullInput() {
        example.processList(null);
        assertEquals("The list is null or empty\n", outContent.toString());
    }

    @Test
    void testProcessListWithEmptyInput() {
        example.processList(Collections.emptyList());
        assertEquals("The list is null or empty\n", outContent.toString());
    }

    @ParameterizedTest
    @CsvSource({"1;2;3, 6", "0, 0", "10;-5;7;3, 15"})
    void testSum(String input, int expected) {
        int[] numbers = Arrays.stream(input.split(";")).mapToInt(Integer::parseInt).toArray();
        assertEquals(expected, example.sum(numbers));
    }
}