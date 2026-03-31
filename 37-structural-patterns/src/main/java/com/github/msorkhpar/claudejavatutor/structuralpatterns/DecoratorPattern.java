package com.github.msorkhpar.claudejavatutor.structuralpatterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Demonstrates the Decorator Pattern — a structural design pattern that lets you attach
 * new behaviors to objects by placing them inside wrapper objects that contain the behaviors.
 * Decorators provide a flexible alternative to subclassing for extending functionality.
 */
public class DecoratorPattern {

    // -----------------------------------------------------------------------
    // Component interface — defines the common interface
    // -----------------------------------------------------------------------

    /**
     * The base component interface that both concrete components and decorators implement.
     */
    public interface Notifier {
        String send(String message);
        String getDescription();
    }

    // -----------------------------------------------------------------------
    // Concrete Component
    // -----------------------------------------------------------------------

    /**
     * The base notifier that sends messages via email.
     */
    public static class EmailNotifier implements Notifier {

        private final String emailAddress;

        public EmailNotifier(String emailAddress) {
            this.emailAddress = Objects.requireNonNull(emailAddress, "Email address must not be null");
            if (emailAddress.isBlank()) {
                throw new IllegalArgumentException("Email address must not be blank");
            }
        }

        @Override
        public String send(String message) {
            Objects.requireNonNull(message, "Message must not be null");
            return "Email to %s: %s".formatted(emailAddress, message);
        }

        @Override
        public String getDescription() {
            return "Email(%s)".formatted(emailAddress);
        }
    }

    // -----------------------------------------------------------------------
    // Base Decorator — implements the component interface and wraps another component
    // -----------------------------------------------------------------------

    /**
     * Abstract base decorator that delegates all calls to the wrapped component.
     * Concrete decorators extend this to add behavior before/after delegation.
     */
    public abstract static class NotifierDecorator implements Notifier {

        protected final Notifier wrappee;

        protected NotifierDecorator(Notifier wrappee) {
            this.wrappee = Objects.requireNonNull(wrappee, "Wrapped notifier must not be null");
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

    // -----------------------------------------------------------------------
    // Concrete Decorators
    // -----------------------------------------------------------------------

    /**
     * Decorator that adds SMS notification capability.
     */
    public static class SmsNotifierDecorator extends NotifierDecorator {

        private final String phoneNumber;

        public SmsNotifierDecorator(Notifier wrappee, String phoneNumber) {
            super(wrappee);
            this.phoneNumber = Objects.requireNonNull(phoneNumber, "Phone number must not be null");
            if (phoneNumber.isBlank()) {
                throw new IllegalArgumentException("Phone number must not be blank");
            }
        }

        @Override
        public String send(String message) {
            Objects.requireNonNull(message, "Message must not be null");
            String base = super.send(message);
            return base + " | SMS to %s: %s".formatted(phoneNumber, message);
        }

        @Override
        public String getDescription() {
            return super.getDescription() + " + SMS(%s)".formatted(phoneNumber);
        }
    }

    /**
     * Decorator that adds Slack notification capability.
     */
    public static class SlackNotifierDecorator extends NotifierDecorator {

        private final String channel;

        public SlackNotifierDecorator(Notifier wrappee, String channel) {
            super(wrappee);
            this.channel = Objects.requireNonNull(channel, "Channel must not be null");
            if (channel.isBlank()) {
                throw new IllegalArgumentException("Channel must not be blank");
            }
        }

        @Override
        public String send(String message) {
            Objects.requireNonNull(message, "Message must not be null");
            String base = super.send(message);
            return base + " | Slack #%s: %s".formatted(channel, message);
        }

        @Override
        public String getDescription() {
            return super.getDescription() + " + Slack(#%s)".formatted(channel);
        }
    }

    /**
     * Decorator that adds logging around message sending.
     */
    public static class LoggingNotifierDecorator extends NotifierDecorator {

        private final List<String> log = new ArrayList<>();

        public LoggingNotifierDecorator(Notifier wrappee) {
            super(wrappee);
        }

        @Override
        public String send(String message) {
            Objects.requireNonNull(message, "Message must not be null");
            log.add("Sending: " + message);
            String result = super.send(message);
            log.add("Sent: " + result);
            return result;
        }

        @Override
        public String getDescription() {
            return "Logging(" + super.getDescription() + ")";
        }

        public List<String> getLog() {
            return Collections.unmodifiableList(log);
        }
    }

    // -----------------------------------------------------------------------
    // Real-world example: I/O stream-style data processing
    // -----------------------------------------------------------------------

    /**
     * Base interface for data processing, similar to Java I/O streams.
     */
    public interface DataSource {
        String read();
        void write(String data);
    }

    /**
     * Concrete component that stores data in memory.
     */
    public static class InMemoryDataSource implements DataSource {

        private String data = "";

        @Override
        public String read() {
            return data;
        }

        @Override
        public void write(String data) {
            this.data = Objects.requireNonNull(data, "Data must not be null");
        }
    }

    /**
     * Decorator that adds encryption/decryption to data operations.
     */
    public static class EncryptionDecorator implements DataSource {

        private final DataSource wrappee;
        private final int shift;

        public EncryptionDecorator(DataSource wrappee, int shift) {
            this.wrappee = Objects.requireNonNull(wrappee, "Data source must not be null");
            this.shift = shift;
        }

        @Override
        public String read() {
            String encrypted = wrappee.read();
            return decrypt(encrypted);
        }

        @Override
        public void write(String data) {
            Objects.requireNonNull(data, "Data must not be null");
            wrappee.write(encrypt(data));
        }

        private String encrypt(String data) {
            StringBuilder sb = new StringBuilder();
            for (char c : data.toCharArray()) {
                sb.append((char) (c + shift));
            }
            return sb.toString();
        }

        private String decrypt(String data) {
            StringBuilder sb = new StringBuilder();
            for (char c : data.toCharArray()) {
                sb.append((char) (c - shift));
            }
            return sb.toString();
        }
    }

    /**
     * Decorator that adds compression (simple run-length encoding for demonstration).
     */
    public static class CompressionDecorator implements DataSource {

        private final DataSource wrappee;

        public CompressionDecorator(DataSource wrappee) {
            this.wrappee = Objects.requireNonNull(wrappee, "Data source must not be null");
        }

        @Override
        public String read() {
            String compressed = wrappee.read();
            return decompress(compressed);
        }

        @Override
        public void write(String data) {
            Objects.requireNonNull(data, "Data must not be null");
            wrappee.write(compress(data));
        }

        String compress(String data) {
            if (data.isEmpty()) return data;
            StringBuilder sb = new StringBuilder();
            int count = 1;
            for (int i = 1; i <= data.length(); i++) {
                if (i < data.length() && data.charAt(i) == data.charAt(i - 1)) {
                    count++;
                } else {
                    sb.append(data.charAt(i - 1));
                    if (count > 1) sb.append(count);
                    count = 1;
                }
            }
            return sb.toString();
        }

        String decompress(String data) {
            if (data.isEmpty()) return data;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < data.length(); i++) {
                char c = data.charAt(i);
                if (Character.isDigit(c)) {
                    // repeat the previous character
                    int repeatCount = Character.getNumericValue(c) - 1; // already added once
                    char prev = sb.charAt(sb.length() - 1);
                    sb.append(String.valueOf(prev).repeat(repeatCount));
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}
