package practice;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ReportResponderTest {

    /** A connection's output: it records close() and refuses writes after it, as a closed socket does. */
    static final class Connection extends ByteArrayOutputStream {
        boolean closed;

        @Override public synchronized void write(int b) {
            if (closed) throw new IllegalStateException("connection closed");
            super.write(b);
        }

        @Override public synchronized void write(byte[] b, int off, int len) {
            if (closed) throw new IllegalStateException("connection closed");
            super.write(b, off, len);
        }

        @Override public void close() { closed = true; }
    }

    private static List<String> stockRows(int n) {
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < n; i++) rows.add("WH-" + (i % 7) + ";SKU-" + (90_000 + i) + ";on-hand=" + (i * 13 % 1000));
        return rows;
    }

    /** Decompresses as much of the body as it can; a truncated body gives the rows before the cut. */
    private static List<String> rowsReadable(byte[] body) throws IOException {
        ByteArrayOutputStream text = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(body))) {
            int n;
            while ((n = in.read(buf)) > 0) text.write(buf, 0, n);
        } catch (EOFException truncated) {
            // keep what was decompressed before the end of the data
        }
        return text.toString(StandardCharsets.UTF_8).lines().toList();
    }

    @Test
    void theBodyIsGzipCompressed() throws IOException {
        Connection connection = new Connection();

        new ReportResponder().send(stockRows(40), connection);

        byte[] body = connection.toByteArray();
        assertThat(body.length).isGreaterThan(2);
        assertThat(body[0]).isEqualTo((byte) 0x1f);
        assertThat(body[1]).isEqualTo((byte) 0x8b);
    }

    @Test
    void theServerCanWriteTheNextMessageOnTheSameConnection() throws IOException {
        Connection connection = new Connection();

        new ReportResponder().send(stockRows(25), connection);

        assertThat(connection.closed).as("connection closed by send()").isFalse();
        assertThatCode(() -> connection.write("NEXT stock-delta 0042\n".getBytes(StandardCharsets.US_ASCII)))
                .doesNotThrowAnyException();
    }

    @Test
    void everyRowReachesTheClientInAShortReportAndALongOne() throws IOException {
        for (int n : new int[] {3, 6_000}) {
            Connection connection = new Connection();
            List<String> rows = stockRows(n);

            new ReportResponder().send(rows, connection);

            assertThat(rowsReadable(connection.toByteArray())).as("rows of a %d-row report", n).isEqualTo(rows);
        }
    }

    @Test
    void theClientCanDecompressTheBodyToItsEnd() throws IOException {
        Connection connection = new Connection();

        new ReportResponder().send(stockRows(6_000), connection);

        assertThatCode(() -> {
            try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(connection.toByteArray()))) {
                in.readAllBytes();
            }
        }).doesNotThrowAnyException();
    }
}
