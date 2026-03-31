package com.github.msorkhpar.claudejavatutor.creationalpatterns;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Factory Method Pattern Tests")
class FactoryMethodPatternTest {

    @Nested
    @DisplayName("Notification Products (Sealed Interface)")
    class NotificationProductsTest {

        @Test
        @DisplayName("Should create EmailNotification with valid recipient")
        void testEmailNotification() {
            var email = new FactoryMethodPattern.EmailNotification("user@example.com");

            assertThat(email.send("Hello")).isEqualTo("Email to user@example.com: Hello");
            assertThat(email.type()).isEqualTo("EMAIL");
            assertThat(email.recipient()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("Should create SmsNotification with valid phone number")
        void testSmsNotification() {
            var sms = new FactoryMethodPattern.SmsNotification("+1234567890");

            assertThat(sms.send("Hello")).isEqualTo("SMS to +1234567890: Hello");
            assertThat(sms.type()).isEqualTo("SMS");
            assertThat(sms.phoneNumber()).isEqualTo("+1234567890");
        }

        @Test
        @DisplayName("Should create PushNotification with valid device token")
        void testPushNotification() {
            var push = new FactoryMethodPattern.PushNotification("device-abc-123");

            assertThat(push.send("Hello")).isEqualTo("Push to device-abc-123: Hello");
            assertThat(push.type()).isEqualTo("PUSH");
            assertThat(push.deviceToken()).isEqualTo("device-abc-123");
        }

        @Test
        @DisplayName("Should throw NullPointerException for null email recipient")
        void testNullEmailRecipient() {
            assertThatThrownBy(() -> new FactoryMethodPattern.EmailNotification(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for blank email recipient")
        void testBlankEmailRecipient() {
            assertThatThrownBy(() -> new FactoryMethodPattern.EmailNotification("  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException for null phone number")
        void testNullPhoneNumber() {
            assertThatThrownBy(() -> new FactoryMethodPattern.SmsNotification(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for blank phone number")
        void testBlankPhoneNumber() {
            assertThatThrownBy(() -> new FactoryMethodPattern.SmsNotification(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException for null device token")
        void testNullDeviceToken() {
            assertThatThrownBy(() -> new FactoryMethodPattern.PushNotification(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for blank device token")
        void testBlankDeviceToken() {
            assertThatThrownBy(() -> new FactoryMethodPattern.PushNotification(" "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should implement sealed Notification interface")
        void testSealedInterface() {
            FactoryMethodPattern.Notification email = new FactoryMethodPattern.EmailNotification("a@b.com");
            FactoryMethodPattern.Notification sms = new FactoryMethodPattern.SmsNotification("123");
            FactoryMethodPattern.Notification push = new FactoryMethodPattern.PushNotification("token");

            assertThat(email).isInstanceOf(FactoryMethodPattern.Notification.class);
            assertThat(sms).isInstanceOf(FactoryMethodPattern.Notification.class);
            assertThat(push).isInstanceOf(FactoryMethodPattern.Notification.class);
        }
    }

    @Nested
    @DisplayName("Classic Factory Method (Creator Hierarchy)")
    class CreatorHierarchyTest {

        @Test
        @DisplayName("EmailNotificationService should create email notifications")
        void testEmailService() {
            var service = new FactoryMethodPattern.EmailNotificationService("admin@company.com");

            String result = service.sendNotification("Server is down");

            assertThat(result).isEqualTo("Email to admin@company.com: Server is down");
        }

        @Test
        @DisplayName("SmsNotificationService should create SMS notifications")
        void testSmsService() {
            var service = new FactoryMethodPattern.SmsNotificationService("+9876543210");

            String result = service.sendNotification("Your code: 1234");

            assertThat(result).isEqualTo("SMS to +9876543210: Your code: 1234");
        }

        @Test
        @DisplayName("PushNotificationService should create push notifications")
        void testPushService() {
            var service = new FactoryMethodPattern.PushNotificationService("device-xyz");

            String result = service.sendNotification("New message");

            assertThat(result).isEqualTo("Push to device-xyz: New message");
        }

        @Test
        @DisplayName("Should describe email notification using pattern matching")
        void testDescribeEmail() {
            var service = new FactoryMethodPattern.EmailNotificationService("test@test.com");

            assertThat(service.describeNotification())
                    .isEqualTo("Email notification to: test@test.com");
        }

        @Test
        @DisplayName("Should describe SMS notification using pattern matching")
        void testDescribeSms() {
            var service = new FactoryMethodPattern.SmsNotificationService("+1111111111");

            assertThat(service.describeNotification())
                    .isEqualTo("SMS notification to: +1111111111");
        }

        @Test
        @DisplayName("Should describe push notification using pattern matching")
        void testDescribePush() {
            var service = new FactoryMethodPattern.PushNotificationService("token-abc");

            assertThat(service.describeNotification())
                    .isEqualTo("Push notification to device: token-abc");
        }

        @Test
        @DisplayName("Factory method should always return correct product type")
        void testFactoryMethodProductType() {
            FactoryMethodPattern.NotificationService emailService =
                    new FactoryMethodPattern.EmailNotificationService("e@e.com");
            FactoryMethodPattern.NotificationService smsService =
                    new FactoryMethodPattern.SmsNotificationService("123");

            assertThat(emailService.createNotification())
                    .isInstanceOf(FactoryMethodPattern.EmailNotification.class);
            assertThat(smsService.createNotification())
                    .isInstanceOf(FactoryMethodPattern.SmsNotification.class);
        }

        @Test
        @DisplayName("Should create new product instance on each factory method call")
        void testNewInstanceEachCall() {
            var service = new FactoryMethodPattern.EmailNotificationService("a@a.com");

            var n1 = service.createNotification();
            var n2 = service.createNotification();

            assertThat(n1).isNotSameAs(n2);
            assertThat(n1).isEqualTo(n2); // Records have value equality
        }

        @Test
        @DisplayName("Polymorphic behavior -- same template method, different products")
        void testPolymorphism() {
            FactoryMethodPattern.NotificationService[] services = {
                    new FactoryMethodPattern.EmailNotificationService("a@b.com"),
                    new FactoryMethodPattern.SmsNotificationService("555"),
                    new FactoryMethodPattern.PushNotificationService("token")
            };

            for (var service : services) {
                String result = service.sendNotification("test");
                assertThat(result).contains("test");
            }
        }
    }

    @Nested
    @DisplayName("Parameterized Factory (Simple Factory)")
    class ParameterizedFactoryTest {

        @Test
        @DisplayName("Should create email notification by type string")
        void testCreateEmail() {
            var notification = FactoryMethodPattern.NotificationFactory.create("email", "user@test.com");

            assertThat(notification).isInstanceOf(FactoryMethodPattern.EmailNotification.class);
            assertThat(notification.type()).isEqualTo("EMAIL");
        }

        @Test
        @DisplayName("Should create SMS notification by type string")
        void testCreateSms() {
            var notification = FactoryMethodPattern.NotificationFactory.create("sms", "+123");

            assertThat(notification).isInstanceOf(FactoryMethodPattern.SmsNotification.class);
        }

        @Test
        @DisplayName("Should create push notification by type string")
        void testCreatePush() {
            var notification = FactoryMethodPattern.NotificationFactory.create("push", "device-1");

            assertThat(notification).isInstanceOf(FactoryMethodPattern.PushNotification.class);
        }

        @Test
        @DisplayName("Should be case-insensitive")
        void testCaseInsensitive() {
            assertThat(FactoryMethodPattern.NotificationFactory.create("EMAIL", "a@b.com"))
                    .isInstanceOf(FactoryMethodPattern.EmailNotification.class);
            assertThat(FactoryMethodPattern.NotificationFactory.create("Email", "a@b.com"))
                    .isInstanceOf(FactoryMethodPattern.EmailNotification.class);
        }

        @Test
        @DisplayName("Should throw for unknown type")
        void testUnknownType() {
            assertThatThrownBy(() -> FactoryMethodPattern.NotificationFactory.create("pigeon", "target"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown notification type");
        }

        @Test
        @DisplayName("Should throw for null type")
        void testNullType() {
            assertThatThrownBy(() -> FactoryMethodPattern.NotificationFactory.create(null, "target"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw for null target")
        void testNullTarget() {
            assertThatThrownBy(() -> FactoryMethodPattern.NotificationFactory.create("email", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Lambda-based Registry Factory")
    class RegistryFactoryTest {

        private FactoryMethodPattern.NotificationRegistry createDefaultRegistry() {
            Map<String, Supplier<FactoryMethodPattern.Notification>> map = new HashMap<>();
            map.put("EMAIL", () -> new FactoryMethodPattern.EmailNotification("default@test.com"));
            map.put("SMS", () -> new FactoryMethodPattern.SmsNotification("+0000000000"));
            map.put("PUSH", () -> new FactoryMethodPattern.PushNotification("default-token"));
            return new FactoryMethodPattern.NotificationRegistry(map);
        }

        @Test
        @DisplayName("Should create registered notification type")
        void testCreateRegistered() {
            var registry = createDefaultRegistry();

            var notification = registry.create("email");

            assertThat(notification).isInstanceOf(FactoryMethodPattern.EmailNotification.class);
        }

        @Test
        @DisplayName("Should be case-insensitive for create")
        void testCaseInsensitive() {
            var registry = createDefaultRegistry();

            assertThat(registry.create("Email"))
                    .isInstanceOf(FactoryMethodPattern.EmailNotification.class);
        }

        @Test
        @DisplayName("Should throw for unregistered type")
        void testUnregisteredType() {
            var registry = createDefaultRegistry();

            assertThatThrownBy(() -> registry.create("fax"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown notification type");
        }

        @Test
        @DisplayName("Should throw for null type in create")
        void testNullType() {
            var registry = createDefaultRegistry();

            assertThatThrownBy(() -> registry.create(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should check if type is supported")
        void testSupports() {
            var registry = createDefaultRegistry();

            assertThat(registry.supports("EMAIL")).isTrue();
            assertThat(registry.supports("sms")).isTrue();
            assertThat(registry.supports("fax")).isFalse();
            assertThat(registry.supports(null)).isFalse();
        }

        @Test
        @DisplayName("Should report correct number of registered types")
        void testRegisteredTypeCount() {
            var registry = createDefaultRegistry();

            assertThat(registry.registeredTypeCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should work with empty registry")
        void testEmptyRegistry() {
            var registry = new FactoryMethodPattern.NotificationRegistry(Map.of());

            assertThat(registry.registeredTypeCount()).isZero();
            assertThat(registry.supports("email")).isFalse();
        }

        @Test
        @DisplayName("Each create call should invoke the Supplier again")
        void testFreshInstancePerCall() {
            var registry = createDefaultRegistry();

            var n1 = registry.create("email");
            var n2 = registry.create("email");

            // Both are EmailNotification with same data, so equal by value
            assertThat(n1).isEqualTo(n2);
        }
    }
}
