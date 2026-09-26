package practice;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Notify {

    private Notify() {
    }

    /** The abstraction, owned by the high-level module. */
    public interface MessageSender {
        void send(String recipient, String message);
    }

    /** A low-level detail. */
    public static final class EmailSender implements MessageSender {
        private final List<String> sent = new CopyOnWriteArrayList<>();

        public void send(String recipient, String message) {
            sent.add("EMAIL[%s]: %s".formatted(recipient, message));
        }

        public List<String> sentMessages() {
            return List.copyOf(sent);
        }
    }

    /** Another low-level detail. */
    public static final class SmsSender implements MessageSender {
        private final List<String> sent = new CopyOnWriteArrayList<>();

        public void send(String recipient, String message) {
            sent.add("SMS[%s]: %s".formatted(recipient, message));
        }

        public List<String> sentMessages() {
            return List.copyOf(sent);
        }
    }

    /** The high-level module: depends on MessageSender only. */
    public static final class NotificationManager {
        private final MessageSender sender;

        public NotificationManager(MessageSender sender) {
            this.sender = Objects.requireNonNull(sender);
        }

        public void notifyUser(String user, String message) {
            if (sender instanceof EmailSender email) {
                email.send(user, message);
            } else if (sender instanceof SmsSender sms) {
                sms.send(user, message);
            }
        }
    }
}
