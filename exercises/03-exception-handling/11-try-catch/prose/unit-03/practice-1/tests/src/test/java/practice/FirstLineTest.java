package practice;

import org.junit.jupiter.api.Test;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirstLineTest {

    /** A Reader whose every read fails. */
    private static final class FailingReader extends FilterReader {

        FailingReader() {
            super(new StringReader("unused"));
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            throw new IOException("read failed");
        }
    }

    /** A closed StringReader (or a reader over one) refuses ready() with an IOException. */
    private static boolean isClosed(Reader reader) {
        try {
            reader.ready();
            return false;
        } catch (IOException closed) {
            return true;
        }
    }

    @Test
    void readsTheFirstLineAndCloses() throws IOException {
        StringReader source = new StringReader("first\nsecond");
        assertThat(FirstLine.read(source)).isEqualTo("first");
        assertThat(isClosed(source)).isTrue();
        assertThat(FirstLine.read(new StringReader("only"))).isEqualTo("only");
    }

    @Test
    void closesWhenReadingFails() {
        FailingReader source = new FailingReader();
        assertThatThrownBy(() -> FirstLine.read(source))
                .isInstanceOf(IOException.class)
                .hasMessage("read failed");
        assertThat(isClosed(source)).isTrue();
    }

    @Test
    void anEmptySourceGivesNull() throws IOException {
        StringReader source = new StringReader("");
        assertThat(FirstLine.read(source)).isNull();
        assertThat(isClosed(source)).isTrue();
    }
}
