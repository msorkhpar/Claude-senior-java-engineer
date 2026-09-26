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
            if (!(this instanceof EmailFactory) && !(this instanceof SmsFactory)) {
                throw new IllegalArgumentException("unknown channel: " + getClass().getName());
            }
            Notification notification = createNotification();
            if (notification == null) {
                throw new IllegalStateException(getClass().getSimpleName() + " created no notification");
            }
            return notification.send(message);
        }
    }

    public static final class EmailFactory extends NotificationFactory {
        @Override
        protected Notification createNotification() {
            return message -> "Email: " + message;
        }
    }

    public static final class SmsFactory extends NotificationFactory {
        @Override
        protected Notification createNotification() {
            return message -> "SMS: " + message;
        }
    }
}
