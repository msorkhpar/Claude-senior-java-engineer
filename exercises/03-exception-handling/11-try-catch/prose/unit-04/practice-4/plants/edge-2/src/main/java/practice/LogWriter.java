package practice;

import java.io.IOException;

public final class LogWriter {

    private LogWriter() {
    }

    /** A writable channel. */
    public interface Channel extends AutoCloseable {
        void write(String text) throws IOException;

        @Override
        void close() throws IOException;
    }

    /** Opens a channel by name. */
    public interface Opener {
        Channel open(String name) throws IOException;
    }

    /** Writes {@code text} to the "log" channel and reports "ok" or the failure with what it suppressed. */
    public static String write(Opener opener, String text) {
        Channel channel = null;
        try {
            channel = opener.open("log");
            channel.write(text);
            return "ok";
        } catch (IOException e) {
            return "failed: " + e.getMessage();
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ignored) {
                    // closing quietly
                }
            }
        }
    }
}
