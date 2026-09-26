package practice;

import java.util.List;

public final class Notify {

    private Notify() {
    }

    /** The abstraction, owned by the high-level module. */
    public interface MessageSender {
        void send(String recipient, String message);
    }

    /** A low-level detail. */
    public static final class EmailSender implements MessageSender {

        public void send(String recipient, String message) {
            throw new UnsupportedOperationException("write send");
        }

        public List<String> sentMessages() {
            throw new UnsupportedOperationException("write sentMessages");
        }
    }

    /** Another low-level detail. */
    public static final class SmsSender implements MessageSender {

        public void send(String recipient, String message) {
            throw new UnsupportedOperationException("write send");
        }

        public List<String> sentMessages() {
            throw new UnsupportedOperationException("write sentMessages");
        }
    }

    /** The high-level module. */
    public static final class NotificationManager {

        public NotificationManager(MessageSender sender) {
            throw new UnsupportedOperationException("write the constructor");
        }

        public void notifyUser(String user, String message) {
            throw new UnsupportedOperationException("write notifyUser");
        }
    }
}
