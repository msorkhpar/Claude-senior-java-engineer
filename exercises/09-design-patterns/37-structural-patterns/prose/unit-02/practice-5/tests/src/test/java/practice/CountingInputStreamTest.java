package practice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CountingInputStreamTest {

    private static InputStream hello() {
        return new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void passesBytesThroughAndCountsThem() throws IOException {
        CountingInputStream in = new CountingInputStream(hello());

        StringBuilder seen = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            seen.append((char) in.read());
        }

        assertThat(seen.toString()).isEqualTo("hello");
        assertThat(in.getCount()).isEqualTo(5);
    }

    @Test
    void aBulkReadCountsWhatItReturned() throws IOException {
        CountingInputStream in = new CountingInputStream(hello());
        byte[] buffer = new byte[10];

        int n = in.read(buffer, 0, 10);

        assertThat(n).isEqualTo(5);
        assertThat(new String(buffer, 0, n, StandardCharsets.UTF_8)).isEqualTo("hello");
        assertThat(in.getCount()).isEqualTo(5);
    }

    @Test
    void theEndOfStreamIsNotCounted() throws IOException {
        CountingInputStream in = new CountingInputStream(hello());

        while (in.read() != -1) {
            // drain
        }
        assertThat(in.read()).isEqualTo(-1);

        assertThat(in.getCount()).isEqualTo(5);
    }

    @Test
    void everyReadPathIsCounted() throws IOException {
        CountingInputStream all = new CountingInputStream(hello());
        assertThat(all.readAllBytes()).hasSize(5);
        assertThat(all.getCount()).isEqualTo(5);

        byte[] text = "abc".repeat(400).getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream packed = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(packed)) {
            gz.write(text);
        }
        byte[] compressed = packed.toByteArray();

        CountingInputStream counter = new CountingInputStream(new ByteArrayInputStream(compressed));
        try (GZIPInputStream unzip = new GZIPInputStream(counter)) {
            assertThat(unzip.readAllBytes()).isEqualTo(text);
        }
        assertThat(counter.getCount()).isEqualTo(compressed.length);
    }
}
