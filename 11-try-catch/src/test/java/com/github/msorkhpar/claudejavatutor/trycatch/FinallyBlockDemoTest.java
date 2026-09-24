package com.github.msorkhpar.claudejavatutor.trycatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FinallyBlockDemoTest {

    @Test
    void testReadFirstLineFromFile(@TempDir Path tempDir) throws IOException {
        Path tempFile = tempDir.resolve("test.txt");
        Files.writeString(tempFile, "Test content\nSecond line\n");

        String result = FinallyBlockDemo.readFirstLineFromFile(tempFile.toString());
        assertThat(result).isEqualTo("Test content");
    }

    @Test
    void testReadFirstLineFromEmptyFileReturnsNull(@TempDir Path tempDir) throws IOException {
        Path emptyFile = Files.createFile(tempDir.resolve("empty.txt"));

        assertThat(FinallyBlockDemo.readFirstLineFromFile(emptyFile.toString())).isNull();
    }

    @Test
    void testReadFirstLineFromMissingFileThrows(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("does-not-exist.txt");

        assertThatThrownBy(() -> FinallyBlockDemo.readFirstLineFromFile(missing.toString()))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    void testReadFirstLineClosesTheSourceOnSuccess() throws IOException {
        TrackingReader source = new TrackingReader(new StringReader("first\nsecond"), false);

        assertThat(FinallyBlockDemo.readFirstLine(source)).isEqualTo("first");
        assertThat(source.closed).isTrue();
    }

    @Test
    void testReadFirstLineClosesTheSourceWhenReadingFails() {
        TrackingReader source = new TrackingReader(new StringReader("unused"), true);

        assertThatThrownBy(() -> FinallyBlockDemo.readFirstLine(source))
                .isInstanceOf(IOException.class)
                .hasMessage("read failed");
        assertThat(source.closed).isTrue();
    }

    /** A Reader that records whether close() was called and can be told to fail on read. */
    private static final class TrackingReader extends FilterReader {
        private final boolean failOnRead;
        private boolean closed;

        TrackingReader(Reader in, boolean failOnRead) {
            super(in);
            this.failOnRead = failOnRead;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (failOnRead) {
                throw new IOException("read failed");
            }
            return super.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
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