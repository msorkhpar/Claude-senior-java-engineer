package com.github.msorkhpar.claudejavatutor.structuralpatterns;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Decorator Pattern Tests")
class DecoratorPatternTest {

    @Nested
    @DisplayName("EmailNotifier (Concrete Component)")
    class EmailNotifierTest {

        @Test
        @DisplayName("Should send email notification")
        void testSendEmail() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");

            String result = notifier.send("Hello");

            assertThat(result).isEqualTo("Email to user@example.com: Hello");
        }

        @Test
        @DisplayName("Should return correct description")
        void testGetDescription() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");

            assertThat(notifier.getDescription()).isEqualTo("Email(user@example.com)");
        }

        @Test
        @DisplayName("Should throw NullPointerException for null email")
        void testNullEmail() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DecoratorPattern.EmailNotifier(null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for blank email")
        void testBlankEmail() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new DecoratorPattern.EmailNotifier("   "));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null message")
        void testNullMessage() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");

            assertThatNullPointerException()
                    .isThrownBy(() -> notifier.send(null));
        }
    }

    @Nested
    @DisplayName("SmsNotifierDecorator")
    class SmsNotifierDecoratorTest {

        @Test
        @DisplayName("Should add SMS notification to email")
        void testSmsDecoration() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");
            var smsDecorator = new DecoratorPattern.SmsNotifierDecorator(notifier, "+1234567890");

            String result = smsDecorator.send("Alert!");

            assertThat(result).contains("Email to user@example.com: Alert!");
            assertThat(result).contains("SMS to +1234567890: Alert!");
        }

        @Test
        @DisplayName("Should return combined description")
        void testDescription() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");
            var smsDecorator = new DecoratorPattern.SmsNotifierDecorator(notifier, "+1234567890");

            assertThat(smsDecorator.getDescription())
                    .isEqualTo("Email(user@example.com) + SMS(+1234567890)");
        }

        @Test
        @DisplayName("Should throw NullPointerException for null wrappee")
        void testNullWrappee() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DecoratorPattern.SmsNotifierDecorator(null, "+1234567890"));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null phone number")
        void testNullPhoneNumber() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");

            assertThatNullPointerException()
                    .isThrownBy(() -> new DecoratorPattern.SmsNotifierDecorator(notifier, null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for blank phone number")
        void testBlankPhoneNumber() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new DecoratorPattern.SmsNotifierDecorator(notifier, "  "));
        }
    }

    @Nested
    @DisplayName("SlackNotifierDecorator")
    class SlackNotifierDecoratorTest {

        @Test
        @DisplayName("Should add Slack notification to email")
        void testSlackDecoration() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");
            var slackDecorator = new DecoratorPattern.SlackNotifierDecorator(notifier, "general");

            String result = slackDecorator.send("Update");

            assertThat(result).contains("Email to user@example.com: Update");
            assertThat(result).contains("Slack #general: Update");
        }

        @Test
        @DisplayName("Should return combined description")
        void testDescription() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");
            var slackDecorator = new DecoratorPattern.SlackNotifierDecorator(notifier, "general");

            assertThat(slackDecorator.getDescription())
                    .isEqualTo("Email(user@example.com) + Slack(#general)");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for blank channel")
        void testBlankChannel() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new DecoratorPattern.SlackNotifierDecorator(notifier, ""));
        }
    }

    @Nested
    @DisplayName("Stacking Multiple Decorators")
    class StackedDecoratorsTest {

        @Test
        @DisplayName("Should stack SMS and Slack decorators on email")
        void testDoubleDecoration() {
            DecoratorPattern.Notifier notifier = new DecoratorPattern.EmailNotifier("user@example.com");
            notifier = new DecoratorPattern.SmsNotifierDecorator(notifier, "+1234567890");
            notifier = new DecoratorPattern.SlackNotifierDecorator(notifier, "alerts");

            String result = notifier.send("Critical!");

            assertThat(result).contains("Email to user@example.com: Critical!");
            assertThat(result).contains("SMS to +1234567890: Critical!");
            assertThat(result).contains("Slack #alerts: Critical!");
        }

        @Test
        @DisplayName("Should stack three decorators and show combined description")
        void testTripleDecorationDescription() {
            DecoratorPattern.Notifier notifier = new DecoratorPattern.EmailNotifier("admin@co.com");
            notifier = new DecoratorPattern.SmsNotifierDecorator(notifier, "+111");
            notifier = new DecoratorPattern.SlackNotifierDecorator(notifier, "ops");

            assertThat(notifier.getDescription())
                    .isEqualTo("Email(admin@co.com) + SMS(+111) + Slack(#ops)");
        }

        @Test
        @DisplayName("Should be usable through the Notifier interface regardless of decoration")
        void testPolymorphicUsage() {
            DecoratorPattern.Notifier base = new DecoratorPattern.EmailNotifier("user@example.com");
            DecoratorPattern.Notifier decorated = new DecoratorPattern.SmsNotifierDecorator(base, "+1");

            // Both are Notifier
            assertThat(base).isInstanceOf(DecoratorPattern.Notifier.class);
            assertThat(decorated).isInstanceOf(DecoratorPattern.Notifier.class);
        }
    }

    @Nested
    @DisplayName("LoggingNotifierDecorator")
    class LoggingNotifierDecoratorTest {

        @Test
        @DisplayName("Should log send operations")
        void testLogging() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");
            var logging = new DecoratorPattern.LoggingNotifierDecorator(notifier);

            logging.send("Test message");

            assertThat(logging.getLog()).hasSize(2);
            assertThat(logging.getLog().get(0)).isEqualTo("Sending: Test message");
            assertThat(logging.getLog().get(1)).contains("Sent:");
        }

        @Test
        @DisplayName("Should accumulate log entries across multiple sends")
        void testMultipleSends() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");
            var logging = new DecoratorPattern.LoggingNotifierDecorator(notifier);

            logging.send("First");
            logging.send("Second");

            assertThat(logging.getLog()).hasSize(4);
        }

        @Test
        @DisplayName("Should return unmodifiable log")
        void testUnmodifiableLog() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");
            var logging = new DecoratorPattern.LoggingNotifierDecorator(notifier);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> logging.getLog().add("hacked"));
        }

        @Test
        @DisplayName("Should show Logging in description")
        void testDescription() {
            var notifier = new DecoratorPattern.EmailNotifier("user@example.com");
            var logging = new DecoratorPattern.LoggingNotifierDecorator(notifier);

            assertThat(logging.getDescription()).isEqualTo("Logging(Email(user@example.com))");
        }
    }

    @Nested
    @DisplayName("DataSource Decorators - Real-world I/O Example")
    class DataSourceDecoratorTest {

        @Test
        @DisplayName("Should write and read plain data")
        void testInMemoryDataSource() {
            var source = new DecoratorPattern.InMemoryDataSource();

            source.write("Hello, World!");

            assertThat(source.read()).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should encrypt and decrypt data transparently")
        void testEncryptionDecorator() {
            var source = new DecoratorPattern.InMemoryDataSource();
            var encrypted = new DecoratorPattern.EncryptionDecorator(source, 3);

            encrypted.write("Hello");

            // The underlying data should be encrypted (shifted)
            assertThat(source.read()).isNotEqualTo("Hello");
            // But reading through the decorator should decrypt it
            assertThat(encrypted.read()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should compress and decompress data transparently")
        void testCompressionDecorator() {
            var source = new DecoratorPattern.InMemoryDataSource();
            var compressed = new DecoratorPattern.CompressionDecorator(source);

            compressed.write("aaabbbccc");

            // Compressed form should be shorter
            assertThat(source.read()).isEqualTo("a3b3c3");
            // Reading through the decorator should decompress
            assertThat(compressed.read()).isEqualTo("aaabbbccc");
        }

        @Test
        @DisplayName("Should stack encryption on compression")
        void testStackedDataSourceDecorators() {
            var source = new DecoratorPattern.InMemoryDataSource();
            var compressed = new DecoratorPattern.CompressionDecorator(source);
            var encrypted = new DecoratorPattern.EncryptionDecorator(compressed, 1);

            encrypted.write("aaabbb");

            // Reading through the full decorator stack should return original data
            assertThat(encrypted.read()).isEqualTo("aaabbb");
        }

        @Test
        @DisplayName("Should handle empty string in compression")
        void testCompressionEmpty() {
            var source = new DecoratorPattern.InMemoryDataSource();
            var compressed = new DecoratorPattern.CompressionDecorator(source);

            compressed.write("");

            assertThat(compressed.read()).isEmpty();
        }

        @Test
        @DisplayName("Should handle single character in compression")
        void testCompressionSingleChar() {
            var source = new DecoratorPattern.InMemoryDataSource();
            var compressed = new DecoratorPattern.CompressionDecorator(source);

            compressed.write("a");

            assertThat(compressed.read()).isEqualTo("a");
        }

        @Test
        @DisplayName("Should handle no repeated characters in compression")
        void testCompressionNoRepeats() {
            var source = new DecoratorPattern.InMemoryDataSource();
            var compressed = new DecoratorPattern.CompressionDecorator(source);

            compressed.write("abcdef");

            assertThat(compressed.read()).isEqualTo("abcdef");
        }

        @Test
        @DisplayName("Should throw NullPointerException for null data on write")
        void testNullDataWrite() {
            var source = new DecoratorPattern.InMemoryDataSource();

            assertThatNullPointerException()
                    .isThrownBy(() -> source.write(null));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null wrappee in EncryptionDecorator")
        void testNullWrappeeEncryption() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DecoratorPattern.EncryptionDecorator(null, 3));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null wrappee in CompressionDecorator")
        void testNullWrappeeCompression() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DecoratorPattern.CompressionDecorator(null));
        }
    }
}
