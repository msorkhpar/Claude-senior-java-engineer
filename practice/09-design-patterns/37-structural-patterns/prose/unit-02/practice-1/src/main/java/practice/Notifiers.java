package practice;

import java.util.Objects;

public final class Notifiers {

    private Notifiers() {
    }

    public interface Notifier {
        String send(String message);

        String getDescription();
    }

    /** The concrete component. */
    public static final class EmailNotifier implements Notifier {
        private final String address;

        public EmailNotifier(String address) {
            this.address = Objects.requireNonNull(address, "address must not be null");
        }

        @Override
        public String send(String message) {
            return "Email to " + address + ": " + message;
        }

        @Override
        public String getDescription() {
            return "Email(" + address + ")";
        }
    }

    /** The base decorator: keeps the wrapped notifier and passes every call on. */
    public abstract static class NotifierDecorator implements Notifier {

        protected NotifierDecorator(Notifier wrappee) {
            throw new UnsupportedOperationException("write the constructor");
        }

        @Override
        public String send(String message) {
            throw new UnsupportedOperationException("write send");
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("write getDescription");
        }
    }

    public static final class SmsDecorator extends NotifierDecorator {

        public SmsDecorator(Notifier wrappee, String phone) {
            super(wrappee);
        }
    }

    public static final class SlackDecorator extends NotifierDecorator {

        public SlackDecorator(Notifier wrappee, String channel) {
            super(wrappee);
        }
    }
}
