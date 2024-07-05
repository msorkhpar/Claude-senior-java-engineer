package com.github.msorkhpar.claudejavatutor.exceptions;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class ExceptionDemoTest {

    @Test
    void testMethodWithCheckedException_NoException() {
        assertThatCode(() -> ExceptionDemo.methodWithCheckedException(false))
                .doesNotThrowAnyException();
    }

    @Test
    void testMethodWithCheckedException_ThrowsException() {
        assertThatThrownBy(() -> ExceptionDemo.methodWithCheckedException(true))
                .isInstanceOf(ExceptionDemo.CustomCheckedException.class)
                .hasMessage("This is a custom checked exception");
    }

    @Test
    void testMethodWithUncheckedException_NoException() {
        assertThatCode(() -> ExceptionDemo.methodWithUncheckedException(false))
                .doesNotThrowAnyException();
    }

    @Test
    void testMethodWithUncheckedException_ThrowsException() {
        assertThatThrownBy(() -> ExceptionDemo.methodWithUncheckedException(true))
                .isInstanceOf(ExceptionDemo.CustomUncheckedException.class)
                .hasMessage("This is a custom unchecked exception");
    }

    @Test
    void testMethodWithExceptionChaining() {
        assertThatThrownBy(() -> ExceptionDemo.methodWithExceptionChaining())
                .isInstanceOf(ExceptionDemo.CustomCheckedException.class)
                .hasMessage("Wrapped exception")
                .hasCauseInstanceOf(IOException.class)
                .hasRootCauseMessage("Original IO exception");
    }
}