package com.github.msorkhpar.claudejavatutor.trycatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class ExceptionHandlingExampleTest {

    private ExceptionHandlingExample example;

    @BeforeEach
    void setUp() {
        example = new ExceptionHandlingExample();
    }

    @Test
    void divideNumbers_normalCase_shouldReturnCorrectResult() {
        assertThat(example.divideNumbers(10, 2)).isEqualTo(5);
    }

    @Test
    void divideNumbers_divideByZero_shouldReturnZero() {
        assertThat(example.divideNumbers(10, 0)).isEqualTo(0);
    }

    @Test
    void getStringLength_normalCase_shouldReturnCorrectLength() {
        assertThat(example.getStringLength("test")).isEqualTo(4);
    }

    @Test
    void getStringLength_nullString_shouldReturnNegativeOne() {
        assertThat(example.getStringLength(null)).isEqualTo(-1);
    }

    @Test
    void demonstrateExceptionChaining_shouldThrowCustomExceptionWithCause() {
        assertThatThrownBy(() -> example.demonstrateExceptionChaining())
                .isInstanceOf(ExceptionHandlingExample.CustomException.class)
                .hasMessageContaining("Error in file processing")
                .hasCauseInstanceOf(IOException.class);
    }
}