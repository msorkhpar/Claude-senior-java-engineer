package practice;

public final class Notifications {

    private Notifications() {
    }

    public interface Notification {
        String send(String message);
    }

    public abstract static class NotificationFactory {

        /** The factory method: each subclass decides the product. */
        protected abstract Notification createNotification();

        /** Sends the message through the product the factory method creates. */
        public final String notify(String message) {
            throw new UnsupportedOperationException("write notify");
        }
    }

    public static final class EmailFactory extends NotificationFactory {
        @Override
        protected Notification createNotification() {
            throw new UnsupportedOperationException("write createNotification");
        }
    }

    public static final class SmsFactory extends NotificationFactory {
        @Override
        protected Notification createNotification() {
            throw new UnsupportedOperationException("write createNotification");
        }
    }
}
