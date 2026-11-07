package practice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Reads lines from a remote log stream on a thread of its own and hands each line on. The stream
 * is a socket's: a read waits until the remote side sends a line, closes, or fails.
 */
public class LogTailer {

    private final InputStream stream;
    private final Consumer<String> lines;
    private final Consumer<IOException> failures;
    private Thread reader;

    /** Lines go to {@code lines}; a stream that fails is reported to {@code failures}, which pages on-call. */
    public LogTailer(InputStream stream, Consumer<String> lines, Consumer<IOException> failures) {
        this.stream = stream;
        this.lines = lines;
        this.failures = failures;
    }

    public void start() {
        reader = new Thread(this::readLines, "log-tailer");
        reader.setDaemon(true);
        reader.start();
    }

    /** Stops the tailer. Returns once its reader thread has ended. */
    public void stop() throws InterruptedException {
        // TODO: Usually the reader thread is waiting in a read when this is called, and the
        //       remote side may not send anything for hours. Add any field you need.
        throw new IllegalStateException("TODO: stop");
    }

    void readLines() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                lines.accept(line);
            }
        } catch (IOException e) {
            readFailed(e);
        }
    }

    /** Called on the reader thread when reading the stream throws. */
    void readFailed(IOException e) {
        // TODO: A broken stream must reach on-call through `failures`. A tailer that is being
        //       stopped must not wake anyone up.
        throw new IllegalStateException("TODO: readFailed");
    }
}
