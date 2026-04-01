package com.github.msorkhpar.claudejavatutor.javasecurity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Secure Coding Practices Tests")
class SecureCodingPracticesTest {

    private final SecureCodingPractices practices = new SecureCodingPractices();

    @Nested
    @DisplayName("Email Validation")
    class EmailValidationTest {

        @ParameterizedTest
        @ValueSource(strings = {"user@example.com", "test.user@domain.org", "a+b@c.co"})
        @DisplayName("Should accept valid emails")
        void testValidEmails(String email) {
            assertThat(practices.validateEmail(email)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid", "@domain.com", "user@", "user@.com", "user@domain"})
        @DisplayName("Should reject invalid emails")
        void testInvalidEmails(String email) {
            assertThat(practices.validateEmail(email)).isFalse();
        }

        @Test
        @DisplayName("Should throw on null email")
        void testNullEmail() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> practices.validateEmail(null))
                    .withMessageContaining("null or blank");
        }

        @Test
        @DisplayName("Should throw on blank email")
        void testBlankEmail() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> practices.validateEmail("   "))
                    .withMessageContaining("null or blank");
        }
    }

    @Nested
    @DisplayName("Username Validation")
    class UsernameValidationTest {

        @Test
        @DisplayName("Should accept valid alphanumeric usernames")
        void testValidUsernames() {
            assertThat(practices.validateUsername("alice")).isTrue();
            assertThat(practices.validateUsername("Bob123")).isTrue();
            assertThat(practices.validateUsername("usr")).isTrue(); // exactly 3 chars
        }

        @Test
        @DisplayName("Should reject too short usernames")
        void testTooShortUsername() {
            assertThat(practices.validateUsername("ab")).isFalse();
            assertThat(practices.validateUsername("")).isFalse();
        }

        @Test
        @DisplayName("Should reject too long usernames")
        void testTooLongUsername() {
            assertThat(practices.validateUsername("a".repeat(21))).isFalse();
        }

        @Test
        @DisplayName("Should accept exactly 20 character username")
        void testMaxLengthUsername() {
            assertThat(practices.validateUsername("a".repeat(20))).isTrue();
        }

        @Test
        @DisplayName("Should reject usernames with special characters")
        void testSpecialCharacters() {
            assertThat(practices.validateUsername("user@name")).isFalse();
            assertThat(practices.validateUsername("user name")).isFalse();
            assertThat(practices.validateUsername("user-name")).isFalse();
        }

        @Test
        @DisplayName("Should throw on null username")
        void testNullUsername() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> practices.validateUsername(null));
        }
    }

    @Nested
    @DisplayName("HTML Sanitization")
    class HtmlSanitizationTest {

        @Test
        @DisplayName("Should escape HTML special characters")
        void testEscapeHtml() {
            String result = practices.sanitizeHtmlInput("<script>alert('xss')</script>");
            assertThat(result).isEqualTo("&lt;script&gt;alert(&#x27;xss&#x27;)&lt;/script&gt;");
        }

        @Test
        @DisplayName("Should escape ampersand")
        void testEscapeAmpersand() {
            assertThat(practices.sanitizeHtmlInput("a & b")).isEqualTo("a &amp; b");
        }

        @Test
        @DisplayName("Should escape double quotes")
        void testEscapeDoubleQuotes() {
            assertThat(practices.sanitizeHtmlInput("a \"b\" c")).isEqualTo("a &quot;b&quot; c");
        }

        @Test
        @DisplayName("Should return empty string for null input")
        void testNullInput() {
            assertThat(practices.sanitizeHtmlInput(null)).isEmpty();
        }

        @Test
        @DisplayName("Should not modify safe text")
        void testSafeText() {
            assertThat(practices.sanitizeHtmlInput("Hello World")).isEqualTo("Hello World");
        }
    }

    @Nested
    @DisplayName("SQL Injection Detection")
    class SqlInjectionDetectionTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "'; DROP TABLE users; --",
                "admin'--",
                "UNION SELECT * FROM passwords",
                "'; exec xp_cmdshell('dir'); --",
                "1; DELETE FROM users"
        })
        @DisplayName("Should detect SQL injection patterns")
        void testDetectSqlInjection(String input) {
            assertThat(practices.detectSqlInjection(input)).isTrue();
        }

        @Test
        @DisplayName("Should not flag normal input")
        void testNormalInput() {
            assertThat(practices.detectSqlInjection("John Doe")).isFalse();
            assertThat(practices.detectSqlInjection("12345")).isFalse();
        }

        @Test
        @DisplayName("Should return false for null or blank input")
        void testNullOrBlank() {
            assertThat(practices.detectSqlInjection(null)).isFalse();
            assertThat(practices.detectSqlInjection("")).isFalse();
            assertThat(practices.detectSqlInjection("   ")).isFalse();
        }
    }

    @Nested
    @DisplayName("XSS Detection")
    class XssDetectionTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "<script>alert('xss')</script>",
                "javascript:alert(1)",
                "<img src=x onerror=alert(1)>",
                "<iframe src='evil.com'>",
                "onmouseover=alert(1)"
        })
        @DisplayName("Should detect XSS patterns")
        void testDetectXss(String input) {
            assertThat(practices.detectXssAttempt(input)).isTrue();
        }

        @Test
        @DisplayName("Should not flag normal HTML-like text")
        void testNormalText() {
            assertThat(practices.detectXssAttempt("Hello World")).isFalse();
            assertThat(practices.detectXssAttempt("2 < 3 and 5 > 4")).isFalse();
        }

        @Test
        @DisplayName("Should return false for null or blank")
        void testNullOrBlank() {
            assertThat(practices.detectXssAttempt(null)).isFalse();
            assertThat(practices.detectXssAttempt("")).isFalse();
        }
    }

    @Nested
    @DisplayName("Immutable Config")
    class ImmutableConfigTest {

        @Test
        @DisplayName("Should create config with defensive copy")
        void testDefensiveCopy() {
            List<String> origins = new ArrayList<>(List.of("https://example.com", "https://api.example.com"));
            var config = new SecureCodingPractices.ImmutableConfig("MyApp", origins);

            // Modify original list
            origins.add("https://evil.com");

            // Config should not be affected
            assertThat(config.getAllowedOrigins()).hasSize(2);
            assertThat(config.getAllowedOrigins()).doesNotContain("https://evil.com");
        }

        @Test
        @DisplayName("Should return unmodifiable list")
        void testUnmodifiableReturn() {
            var config = new SecureCodingPractices.ImmutableConfig("MyApp", List.of("https://a.com"));
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> config.getAllowedOrigins().add("https://evil.com"));
        }

        @Test
        @DisplayName("Should check origin correctly")
        void testIsOriginAllowed() {
            var config = new SecureCodingPractices.ImmutableConfig("MyApp",
                    List.of("https://a.com", "https://b.com"));
            assertThat(config.isOriginAllowed("https://a.com")).isTrue();
            assertThat(config.isOriginAllowed("https://evil.com")).isFalse();
        }

        @Test
        @DisplayName("Should throw on null application name")
        void testNullAppName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new SecureCodingPractices.ImmutableConfig(null, List.of()));
        }

        @Test
        @DisplayName("Should throw on null origins list")
        void testNullOrigins() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new SecureCodingPractices.ImmutableConfig("App", null));
        }
    }

    @Nested
    @DisplayName("Path Traversal Prevention")
    class PathTraversalTest {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("Should allow valid subpath")
        void testValidPath() {
            Path result = practices.validatePath(tempDir, "file.txt");
            assertThat(result.toString()).startsWith(tempDir.toString());
            assertThat(result.getFileName().toString()).isEqualTo("file.txt");
        }

        @Test
        @DisplayName("Should reject path traversal attempt")
        void testPathTraversal() {
            assertThatExceptionOfType(SecurityException.class)
                    .isThrownBy(() -> practices.validatePath(tempDir, "../../etc/passwd"))
                    .withMessageContaining("Path traversal");
        }

        @Test
        @DisplayName("Should throw on null base path")
        void testNullBasePath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> practices.validatePath(null, "file.txt"));
        }

        @Test
        @DisplayName("Should throw on null user path")
        void testNullUserPath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> practices.validatePath(tempDir, null));
        }
    }

    @Nested
    @DisplayName("Safe File Reading")
    class SafeFileReadingTest {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("Should read file within size limit")
        void testReadWithinLimit() throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Hello, World!");

            String content = practices.readFileSafely(file, 1024);
            assertThat(content).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should reject file exceeding size limit")
        void testExceedSizeLimit() throws IOException {
            Path file = tempDir.resolve("large.txt");
            Files.writeString(file, "A".repeat(1000));

            assertThatExceptionOfType(SecurityException.class)
                    .isThrownBy(() -> practices.readFileSafely(file, 100))
                    .withMessageContaining("exceeds maximum");
        }

        @Test
        @DisplayName("Should throw on non-positive max bytes")
        void testNonPositiveMaxBytes() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> practices.readFileSafely(Path.of("/tmp/test"), 0));
        }

        @Test
        @DisplayName("Should throw on null file path")
        void testNullFilePath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> practices.readFileSafely(null, 1024));
        }
    }

    @Nested
    @DisplayName("Credit Card Masking")
    class CreditCardMaskingTest {

        @Test
        @DisplayName("Should mask credit card showing last 4 digits")
        void testMaskCreditCard() {
            assertThat(practices.maskCreditCard("4111111111111111"))
                    .isEqualTo("****-****-****-1111");
        }

        @Test
        @DisplayName("Should handle credit card with dashes")
        void testMaskWithDashes() {
            assertThat(practices.maskCreditCard("4111-1111-1111-5678"))
                    .isEqualTo("****-****-****-5678");
        }

        @Test
        @DisplayName("Should throw on null card number")
        void testNullCardNumber() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> practices.maskCreditCard(null));
        }

        @Test
        @DisplayName("Should throw on card with fewer than 4 digits")
        void testTooFewDigits() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> practices.maskCreditCard("123"))
                    .withMessageContaining("at least 4 digits");
        }
    }

    @Nested
    @DisplayName("Email Masking")
    class EmailMaskingTest {

        @Test
        @DisplayName("Should mask email local part")
        void testMaskEmail() {
            assertThat(practices.maskEmail("john.doe@example.com"))
                    .isEqualTo("j******e@example.com");
        }

        @Test
        @DisplayName("Should handle short local part")
        void testShortLocalPart() {
            assertThat(practices.maskEmail("ab@example.com"))
                    .isEqualTo("a*@example.com");
        }

        @Test
        @DisplayName("Should throw on null email")
        void testNullEmail() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> practices.maskEmail(null));
        }

        @Test
        @DisplayName("Should throw on email without @")
        void testEmailWithoutAt() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> practices.maskEmail("invalidemail"));
        }
    }

    @Nested
    @DisplayName("Integer Overflow Prevention")
    class IntegerOverflowTest {

        @Test
        @DisplayName("Should add normally when no overflow")
        void testSafeAddNormal() {
            assertThat(practices.safeAdd(100, 200)).isEqualTo(300);
        }

        @Test
        @DisplayName("Should throw on integer overflow")
        void testSafeAddOverflow() {
            assertThatExceptionOfType(ArithmeticException.class)
                    .isThrownBy(() -> practices.safeAdd(Integer.MAX_VALUE, 1));
        }

        @Test
        @DisplayName("Should throw on integer underflow")
        void testSafeAddUnderflow() {
            assertThatExceptionOfType(ArithmeticException.class)
                    .isThrownBy(() -> practices.safeAdd(Integer.MIN_VALUE, -1));
        }

        @Test
        @DisplayName("Should multiply normally when no overflow")
        void testSafeMultiplyNormal() {
            assertThat(practices.safeMultiply(100, 200)).isEqualTo(20000);
        }

        @Test
        @DisplayName("Should throw on multiplication overflow")
        void testSafeMultiplyOverflow() {
            assertThatExceptionOfType(ArithmeticException.class)
                    .isThrownBy(() -> practices.safeMultiply(Integer.MAX_VALUE, 2));
        }
    }
}
