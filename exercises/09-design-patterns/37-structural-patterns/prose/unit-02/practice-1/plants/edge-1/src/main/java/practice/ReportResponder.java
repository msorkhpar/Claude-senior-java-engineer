package practice;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Sends a stock report, gzip-compressed, as one message on a connection that carries many. The
 * connection is owned by the server: once send() returns, it writes the next message on it.
 */
public class ReportResponder {

    /** Writes each row and a newline, as UTF-8, gzip-compressed, into the message body. */
    public void send(List<String> rows, OutputStream body) throws IOException {
        GZIPOutputStream gzip = new GZIPOutputStream(body);
        Writer writer = new BufferedWriter(new OutputStreamWriter(gzip, StandardCharsets.UTF_8));
        for (String row : rows) {
            writer.write(row);
            writer.write('\n');
        }
        complete(writer, gzip);
    }

    /** Ends the compressed body that send() wrote through these two layers. */
    void complete(Writer writer, GZIPOutputStream gzip) throws IOException {
        writer.close();
    }
}
