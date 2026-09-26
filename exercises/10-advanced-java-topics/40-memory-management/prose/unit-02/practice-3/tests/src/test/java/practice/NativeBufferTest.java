package practice;

import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NativeBufferTest {

    private static final Cleaner CLEANER = Cleaner.create();

    @Test
    void tryWithResourcesReleasesTheBuffer() {
        AtomicInteger releases = new AtomicInteger();
        NativeBuffer seen;
        try (NativeBuffer buffer = new NativeBuffer(CLEANER, 16, releases::incrementAndGet)) {
            seen = buffer;
            buffer.write(0, (byte) 7);
            assertThat(buffer.isOpen()).isTrue();
            assertThat(releases).hasValue(0);
        }

        assertThat(releases).hasValue(1);
        assertThat(seen.isOpen()).isFalse();
    }

    @Test
    void closingTwiceReleasesOnce() {
        AtomicInteger releases = new AtomicInteger();
        NativeBuffer buffer = new NativeBuffer(CLEANER, 16, releases::incrementAndGet);

        buffer.close();
        buffer.close();

        assertThat(releases).hasValue(1);
    }

    @Test
    void aClosedBufferRefusesToBeUsed() {
        AtomicInteger releases = new AtomicInteger();
        NativeBuffer buffer = new NativeBuffer(CLEANER, 16, releases::incrementAndGet);
        buffer.close();

        assertThatThrownBy(() -> buffer.write(0, (byte) 1)).isInstanceOf(IllegalStateException.class);
    }
}
