package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LineCounterTest {

    @Test
    void countsNonBlankLines() throws IOException {
        assertThat(LineCounter.countNonBlank(Stream.of("hello", new String(""), "world", "!"))).isEqualTo(3);
        assertThat(LineCounter.countNonBlank(Stream.empty())).isZero();

        Path file = Files.createTempFile("notes", ".txt");
        Path empty = Files.createTempFile("empty", ".txt");
        try {
            Files.write(file, List.of("hello", "", "world", "!"));
            assertThat(LineCounter.countNonBlank(file)).isEqualTo(3);
            assertThat(LineCounter.countNonBlank(empty)).isZero();
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(empty);
        }
    }

    @Test
    void theStreamIsClosed() {
        AtomicBoolean closed = new AtomicBoolean(false);
        Stream<String> lines = Stream.of("a", "", "b").onClose(() -> closed.set(true));

        assertThat(LineCounter.countNonBlank(lines)).isEqualTo(2);
        assertThat(closed).isTrue();
    }

    @Test
    void whitespaceOnlyLinesAreBlank() {
        assertThat(LineCounter.countNonBlank(Stream.of("a", "   ", "\t", " b "))).isEqualTo(2);
    }

    @Test
    void theStreamIsClosedEvenWhenCountingFails() {
        AtomicBoolean closed = new AtomicBoolean(false);
        Stream<String> lines = Stream.of("a", "boom", "b")
                .map(line -> {
                    if (line.equals("boom")) {
                        throw new UncheckedIOException(new IOException("disk read failed"));
                    }
                    return line;
                })
                .onClose(() -> closed.set(true));

        assertThatThrownBy(() -> LineCounter.countNonBlank(lines))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("disk read failed");
        assertThat(closed).isTrue();
    }
}
