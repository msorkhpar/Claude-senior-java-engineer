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

        protected final Notifier wrappee;

        protected NotifierDecorator(Notifier wrappee) {
            this.wrappee = Objects.requireNonNull(wrappee, "wrapped notifier must not be null");
        }

        @Override
        public String send(String message) {
            return wrappee.send(message);
        }

        @Override
        public String getDescription() {
            return wrappee.getDescription();
        }
    }

    public static final class SmsDecorator extends NotifierDecorator {

        private final String phone;

        public SmsDecorator(Notifier wrappee, String phone) {
            super(wrappee);
            this.phone = Objects.requireNonNull(phone, "phone must not be null");
        }

        @Override
        public String send(String message) {
            String base = super.send(message);
            return base.contains(" | SMS to ") ? base : base + " | SMS to " + phone + ": " + message;
        }

        @Override
        public String getDescription() {
            return super.getDescription() + " + SMS(" + phone + ")";
        }
    }

    public static final class SlackDecorator extends NotifierDecorator {

        private final String channel;

        public SlackDecorator(Notifier wrappee, String channel) {
            super(wrappee);
            this.channel = Objects.requireNonNull(channel, "channel must not be null");
        }

        @Override
        public String send(String message) {
            return super.send(message) + " | Slack #" + channel + ": " + message;
        }

        @Override
        public String getDescription() {
            return super.getDescription() + " + Slack(#" + channel + ")";
        }
    }
}
