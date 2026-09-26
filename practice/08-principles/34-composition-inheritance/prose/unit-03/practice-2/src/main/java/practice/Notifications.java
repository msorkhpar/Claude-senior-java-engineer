package practice;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/** A notification service composed from a formatter and a sender it is given. */
public final class Notifications {

    private Notifications() {
    }

    public interface NotificationSender {
        boolean send(String recipient, String message);
    }

    public interface MessageFormatter {
        String format(String template, Map<String, String> variables);
    }

    /** Replaces every ${key} in the template with its value; unknown placeholders stay. */
    public static final class TemplateFormatter implements MessageFormatter {
        @Override
        public String format(String template, Map<String, String> variables) {
            throw new UnsupportedOperationException("write format");
        }
    }

    public static final class NotificationService {
        private final NotificationSender sender;
        private final MessageFormatter formatter;
        private final AtomicInteger successCount = new AtomicInteger();
        private final AtomicInteger failureCount = new AtomicInteger();

        public NotificationService(NotificationSender sender, MessageFormatter formatter) {
            this.sender = Objects.requireNonNull(sender, "sender");
            this.formatter = Objects.requireNonNull(formatter, "formatter");
        }

        /** Formats the template with the injected formatter, sends it, and counts the outcome. */
        public boolean notify(String recipient, String template, Map<String, String> variables) {
            throw new UnsupportedOperationException("write notify");
        }

        public int getSuccessCount() {
            throw new UnsupportedOperationException("write getSuccessCount");
        }

        public int getFailureCount() {
            throw new UnsupportedOperationException("write getFailureCount");
        }
    }
}
