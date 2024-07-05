package com.github.msorkhpar.claudejavatutor.trycatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExceptionHandlerTest {

    @ParameterizedTest
    @CsvSource({
            "'', 'Invalid input: Input is empty'",
            "IOException, 'Database or I/O error: Simulated IOException'",
            "SQLException, 'Database or I/O error: Simulated SQLException'",
            "ValidInput, 'Input processed successfully: ValidInput'"
    })
    void testHandleMultipleExceptions(String input, String expectedOutput) {
        String result = ExceptionHandler.handleMultipleExceptions(input);
        assertThat(result).isEqualTo(expectedOutput);
    }


    @Test
    void testHandleMultipleExceptionsWithNull() {
        String result = ExceptionHandler.handleMultipleExceptions(null);
        assertThat(result).isEqualTo("Invalid input: Input is null");
    }

    @Test
    void testHandleMultipleExceptions_unexpectedException() {
        String result = ExceptionHandler.handleMultipleExceptions("UnexpectedException");
        assertThat(result).startsWith("Input processed successfully:");
    }

    @ParameterizedTest
    @ValueSource(strings = {"10", "5", "100"})
    void testDivideNumber_validInput(String input) {
        double result = ExceptionHandler.divideNumber(input);
        assertThat(result).isGreaterThan(0).isLessThanOrEqualTo(100);
    }

    @Test
    void testDivideNumber_invalidFormat() {
        assertThatThrownBy(() -> ExceptionHandler.divideNumber("abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid number format");
    }

    @Test
    void testDivideNumber_negativeNumber() {
        assertThatThrownBy(() -> ExceptionHandler.divideNumber("-5"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Number must be non-negative");
    }

    @Test
    void testDivideNumber_divideByZero() {
        assertThatThrownBy(() -> ExceptionHandler.divideNumber("0"))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Cannot divide by zero");
    }
}