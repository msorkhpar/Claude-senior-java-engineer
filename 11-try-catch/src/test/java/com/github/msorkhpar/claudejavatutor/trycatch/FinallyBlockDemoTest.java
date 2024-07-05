package com.github.msorkhpar.claudejavatutor.trycatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FinallyBlockDemoTest {

    @Test
    void testReadFirstLineFromFile(@TempDir Path tempDir) throws IOException {
        Path tempFile = tempDir.resolve("test.txt");
        Files.write(tempFile, "Test content".getBytes());

        String result = FinallyBlockDemo.readFirstLineFromFile(tempFile.toString());
        assertThat(result).isEqualTo("First line of the file");
    }

    @Test
    void testDemonstrateFinally() {
        int result = FinallyBlockDemo.demonstrateFinally();
        assertThat(result).isEqualTo(1);
    }

    @Test
    void testDemonstrateExceptionInFinally() {
        assertThatThrownBy(() -> FinallyBlockDemo.demonstrateExceptionInFinally())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Exception from finally block");
    }
}