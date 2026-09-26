package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Logging {

    private Logging() {
    }

    public interface Notifier {
        String send(String message);

        String getDescription();
    }

    public static final class LoggingDecorator implements Notifier {

        private final Notifier wrappee;
        private final List<String> log = new ArrayList<>();

        public LoggingDecorator(Notifier wrappee) {
            this.wrappee = Objects.requireNonNull(wrappee, "wrapped notifier must not be null");
        }

        @Override
        public String send(String message) {
            String result = wrappee.send(message);
            log.add("Sending: " + message);
            log.add("Sent: " + result);
            return result;
        }

        @Override
        public String getDescription() {
            return "Logging(" + wrappee.getDescription() + ")";
        }

        /** The entries so far, oldest first. */
        public List<String> getLog() {
            return Collections.unmodifiableList(log);
        }
    }
}
