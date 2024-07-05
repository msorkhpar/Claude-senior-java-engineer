package com.github.msorkhpar.claudejavatutor.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class CustomExceptionDemoTest {

    private final CustomExceptionDemo demo = new CustomExceptionDemo();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  \t  \n"})
    void validateUsername_shouldThrowInvalidUserDataException_whenUsernameIsInvalid(String username) {
        assertThatThrownBy(() -> demo.validateUsername(username))
                .isInstanceOf(CustomExceptionDemo.InvalidUserDataException.class)
                .hasMessage("Username cannot be null or empty");
    }

    @Test
    void validateUsername_shouldNotThrowException_whenUsernameIsValid() {
        assertThatCode(()->demo.validateUsername("validUsername")).doesNotThrowAnyException();
    }

    @Test
    void processFile_shouldThrowFileProcessingException_whenFileDoesNotExist() {
        assertThatThrownBy(() -> demo.processFile("nonexistent.txt"))
                .isInstanceOf(CustomExceptionDemo.FileProcessingException.class)
                .hasMessageContaining("Error processing file: nonexistent.txt")
                .extracting("fileName")
                .isEqualTo("nonexistent.txt");
    }

    @Test
    void processFile_shouldNotThrowException_whenFileExists() {
        assertThatCode(()->demo.processFile("existing.txt")).doesNotThrowAnyException();
    }
}