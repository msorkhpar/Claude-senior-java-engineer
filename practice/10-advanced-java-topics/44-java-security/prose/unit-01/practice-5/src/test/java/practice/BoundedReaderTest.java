package practice;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoundedReaderTest {

    /** A stream that remembers whether it was closed, and can under-report available(). */
    static final class Upload extends ByteArrayInputStream {
        private final boolean hidesItsSize;
        boolean closed;

        Upload(String text, boolean hidesItsSize) {
            super(text.getBytes(StandardCharsets.UTF_8));
            this.hidesItsSize = hidesItsSize;
        }

        @Override
        public synchronized int available() {
            return hidesItsSize ? 0 : super.available();
        }

        int consumed() {
            return pos;
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    @Test
    void readsTextWithinTheLimit() throws Exception {
        Upload upload = new Upload("hello, world", false);

        assertThat(BoundedReader.read(upload, 100)).isEqualTo("hello, world");
        assertThat(upload.closed).isTrue();
    }

    @Test
    void oneByteOverTheLimitIsRefused() throws Exception {
        assertThat(BoundedReader.read(new Upload("0123456789", false), 10)).isEqualTo("0123456789");
        assertThatThrownBy(() -> BoundedReader.read(new Upload("0123456789A", false), 10))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void theLimitIsEnforcedOnTheBytesRead() {
        Upload upload = new Upload("0123456789".repeat(100), true);

        assertThatThrownBy(() -> BoundedReader.read(upload, 10)).isInstanceOf(SecurityException.class);
        assertThat(upload.consumed()).as("bytes pulled from the stream").isLessThanOrEqualTo(11);
    }

    @Test
    void theStreamIsClosedEvenWhenRefused() {
        Upload upload = new Upload("0123456789".repeat(3), false);

        assertThatThrownBy(() -> BoundedReader.read(upload, 10)).isInstanceOf(SecurityException.class);
        assertThat(upload.closed).isTrue();
    }
}
