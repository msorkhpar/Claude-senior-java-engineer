package practice;

import java.util.List;

public final class Logging {

    private Logging() {
    }

    public interface Notifier {
        String send(String message);

        String getDescription();
    }

    public static final class LoggingDecorator implements Notifier {

        public LoggingDecorator(Notifier wrappee) {
        }

        @Override
        public String send(String message) {
            throw new UnsupportedOperationException("write send");
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("write getDescription");
        }

        /** The entries so far, oldest first. */
        public List<String> getLog() {
            throw new UnsupportedOperationException("write getLog");
        }
    }
}
