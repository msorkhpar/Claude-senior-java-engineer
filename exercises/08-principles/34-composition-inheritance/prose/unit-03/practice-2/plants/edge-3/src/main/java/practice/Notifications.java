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
            StringBuilder out = new StringBuilder();
            int from = 0;
            while (true) {
                int open = template.indexOf("${", from);
                int close = open < 0 ? -1 : template.indexOf('}', open + 2);
                if (close < 0) {
                    return out.append(template, from, template.length()).toString();
                }
                String value = variables.get(template.substring(open + 2, close));
                out.append(template, from, open);
                out.append(value != null ? value : template.substring(open, close + 1));
                if (value != null) {
                    return out.append(template, close + 1, template.length()).toString();
                }
                from = close + 1;
            }
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
            String message = formatter.format(template, variables);
            boolean sent = sender.send(recipient, message);
            if (sent) {
                successCount.incrementAndGet();
            } else {
                failureCount.incrementAndGet();
            }
            return sent;
        }

        public int getSuccessCount() {
            return successCount.get();
        }

        public int getFailureCount() {
            return failureCount.get();
        }
    }
}
