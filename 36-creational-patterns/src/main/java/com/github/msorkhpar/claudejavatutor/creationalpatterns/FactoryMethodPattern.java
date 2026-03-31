package com.github.msorkhpar.claudejavatutor.creationalpatterns;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Demonstrates the Factory Method pattern with multiple approaches:
 * sealed interface products, abstract creator classes, parameterized factories,
 * and lambda-based factories.
 */
public class FactoryMethodPattern {

    // =====================================================
    // Product hierarchy using sealed interface and records
    // =====================================================

    /**
     * Product interface -- sealed to enable exhaustive pattern matching.
     */
    public sealed interface Notification permits EmailNotification, SmsNotification, PushNotification {
        String send(String message);
        String type();
    }

    public record EmailNotification(String recipient) implements Notification {
        public EmailNotification {
            Objects.requireNonNull(recipient, "Recipient cannot be null");
            if (recipient.isBlank()) {
                throw new IllegalArgumentException("Recipient cannot be blank");
            }
        }

        @Override
        public String send(String message) {
            return "Email to " + recipient + ": " + message;
        }

        @Override
        public String type() {
            return "EMAIL";
        }
    }

    public record SmsNotification(String phoneNumber) implements Notification {
        public SmsNotification {
            Objects.requireNonNull(phoneNumber, "Phone number cannot be null");
            if (phoneNumber.isBlank()) {
                throw new IllegalArgumentException("Phone number cannot be blank");
            }
        }

        @Override
        public String send(String message) {
            return "SMS to " + phoneNumber + ": " + message;
        }

        @Override
        public String type() {
            return "SMS";
        }
    }

    public record PushNotification(String deviceToken) implements Notification {
        public PushNotification {
            Objects.requireNonNull(deviceToken, "Device token cannot be null");
            if (deviceToken.isBlank()) {
                throw new IllegalArgumentException("Device token cannot be blank");
            }
        }

        @Override
        public String send(String message) {
            return "Push to " + deviceToken + ": " + message;
        }

        @Override
        public String type() {
            return "PUSH";
        }
    }

    // =====================================================
    // Creator hierarchy (classic Factory Method pattern)
    // =====================================================

    /**
     * Abstract creator declaring the factory method.
     * Contains template logic that depends on the product.
     */
    public static abstract class NotificationService {

        /**
         * Factory method -- subclasses override to return specific Notification types.
         */
        public abstract Notification createNotification();

        /**
         * Template method that uses the factory method.
         */
        public String sendNotification(String message) {
            Notification notification = createNotification();
            return notification.send(message);
        }

        /**
         * Uses pattern matching on sealed interface products.
         */
        public String describeNotification() {
            Notification notification = createNotification();
            return switch (notification) {
                case EmailNotification e -> "Email notification to: " + e.recipient();
                case SmsNotification s -> "SMS notification to: " + s.phoneNumber();
                case PushNotification p -> "Push notification to device: " + p.deviceToken();
            };
        }
    }

    /**
     * Concrete creator for email notifications.
     */
    public static class EmailNotificationService extends NotificationService {
        private final String recipient;

        public EmailNotificationService(String recipient) {
            this.recipient = recipient;
        }

        @Override
        public Notification createNotification() {
            return new EmailNotification(recipient);
        }
    }

    /**
     * Concrete creator for SMS notifications.
     */
    public static class SmsNotificationService extends NotificationService {
        private final String phoneNumber;

        public SmsNotificationService(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        @Override
        public Notification createNotification() {
            return new SmsNotification(phoneNumber);
        }
    }

    /**
     * Concrete creator for push notifications.
     */
    public static class PushNotificationService extends NotificationService {
        private final String deviceToken;

        public PushNotificationService(String deviceToken) {
            this.deviceToken = deviceToken;
        }

        @Override
        public Notification createNotification() {
            return new PushNotification(deviceToken);
        }
    }

    // =====================================================
    // Parameterized Factory Method (Simple Factory variant)
    // =====================================================

    /**
     * Parameterized factory that selects the product type based on input.
     * This is a Simple Factory (not strictly the GoF Factory Method), but commonly
     * seen in practice and interview discussions.
     */
    public static class NotificationFactory {

        public static Notification create(String type, String target) {
            Objects.requireNonNull(type, "Type cannot be null");
            Objects.requireNonNull(target, "Target cannot be null");

            return switch (type.toUpperCase()) {
                case "EMAIL" -> new EmailNotification(target);
                case "SMS" -> new SmsNotification(target);
                case "PUSH" -> new PushNotification(target);
                default -> throw new IllegalArgumentException("Unknown notification type: " + type);
            };
        }
    }

    // =====================================================
    // Lambda-based Factory (modern approach)
    // =====================================================

    /**
     * Registry-based factory using Supplier lambdas.
     * New product types can be registered without modifying existing code.
     */
    public static class NotificationRegistry {
        private final Map<String, Supplier<Notification>> registry;

        public NotificationRegistry(Map<String, Supplier<Notification>> registry) {
            this.registry = Map.copyOf(registry);
        }

        public Notification create(String type) {
            Objects.requireNonNull(type, "Type cannot be null");
            Supplier<Notification> supplier = registry.get(type.toUpperCase());
            if (supplier == null) {
                throw new IllegalArgumentException("Unknown notification type: " + type);
            }
            return supplier.get();
        }

        public boolean supports(String type) {
            return type != null && registry.containsKey(type.toUpperCase());
        }

        public int registeredTypeCount() {
            return registry.size();
        }
    }
}
