package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class TextLengthsTest {

    private static final IOException DISK = new IOException("disk");

    private static String fetch(String name) throws IOException {
        if (name.equals("b.txt")) {
            throw DISK;
        }
        return Map.of("a.txt", "abc", "c.txt", "hello").get(name);
    }

    @Test
    void measuresEveryText() {
        assertThat(TextLengths.lengths(List.of("a.txt", "c.txt"), TextLengthsTest::fetch)).containsExactly(3, 5);
        assertThat(TextLengths.lengths(List.of(), TextLengthsTest::fetch)).isEmpty();
    }

    @Test
    void aFailedFetchIsAnUncheckedIOException() {
        UncheckedIOException e = catchThrowableOfType(
                () -> TextLengths.lengths(List.of("a.txt", "b.txt"), TextLengthsTest::fetch),
                UncheckedIOException.class);
        assertThat(e).isNotNull();
        assertThat(e.getCause()).isSameAs(DISK);
    }

    @Test
    void theMessageNamesTheFailedText() {
        UncheckedIOException e = catchThrowableOfType(
                () -> TextLengths.lengths(List.of("a.txt", "b.txt"), TextLengthsTest::fetch),
                UncheckedIOException.class);
        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("Could not read b.txt");
    }
}
