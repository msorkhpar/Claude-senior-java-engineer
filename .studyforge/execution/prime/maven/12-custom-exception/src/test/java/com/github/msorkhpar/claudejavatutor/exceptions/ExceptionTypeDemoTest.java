package com.github.msorkhpar.claudejavatutor.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExceptionTypeDemoTest {

    private final ExceptionTypeDemo demo = new ExceptionTypeDemo();

    @Test
    void testMethodWithCheckedException() {
        assertThatThrownBy(() -> demo.methodWithCheckedException())
                .isInstanceOf(IOException.class)
                .hasMessage("This is a checked exception");
    }

    @Test
    void testMethodWithUncheckedException() {
        assertThatThrownBy(() -> demo.methodWithUncheckedException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This is an unchecked exception");
    }

    @Test
    void testExceptionTranslationDemo() {
        assertThatThrownBy(() -> demo.exceptionTranslationDemo())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Translated exception")
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void testReadFirstLineFromFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.write(file, "Test content".getBytes());

        String result = demo.readFirstLineFromFile(file.toString());
        assertThat(result).isEqualTo("Test content");
    }

    @Test
    void testReadFirstLineFromNonExistentFile() {
        assertThatThrownBy(() -> demo.readFirstLineFromFile("non_existent_file.txt"))
                .isInstanceOf(IOException.class);
    }
}