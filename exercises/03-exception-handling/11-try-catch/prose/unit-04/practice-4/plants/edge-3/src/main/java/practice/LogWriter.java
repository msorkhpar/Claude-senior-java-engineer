package practice;

import java.io.IOException;

public final class LogWriter {

    private LogWriter() {
    }

    public interface Channel extends AutoCloseable {
        void write(String text) throws IOException;

        @Override
        void close() throws IOException;
    }

    public interface Opener {
        Channel open(String name) throws IOException;
    }

    public static String write(Opener opener, String text) {
        try (Channel channel = opener.open("log")) {
            channel.write(text);
            return "ok";
        } catch (Exception e) {
            StringBuilder report = new StringBuilder("failed: ").append(e.getMessage());
            for (Throwable suppressed : e.getSuppressed()) {
                report.append("; suppressed: ").append(suppressed.getMessage());
            }
            return report.toString();
        }
    }
}
