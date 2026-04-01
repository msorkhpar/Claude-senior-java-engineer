package com.github.msorkhpar.claudejavatutor.javasecurity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Demonstrates security best practices in Java including input validation,
 * secure coding guidelines, and secure configuration principles.
 */
public class SecureCodingPractices {

    // ---- Input Validation and Sanitization ----

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern ALPHANUMERIC_PATTERN =
            Pattern.compile("^[A-Za-z0-9]+$");

    private static final Pattern SQL_INJECTION_PATTERN =
            Pattern.compile("(?i)(--|;|'|/\\*|\\*/|xp_|exec|execute|insert|select|delete|update|drop|alter|create|union|into|load_file|outfile)");

    private static final Pattern XSS_PATTERN =
            Pattern.compile("(?i)(<script|javascript:|on\\w+=|<iframe|<object|<embed|<form|<img[^>]+onerror)");

    /**
     * Validates an email address against a strict pattern.
     *
     * @param email the email to validate
     * @return true if valid
     * @throws IllegalArgumentException if email is null or blank
     */
    public boolean validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates a username: must be alphanumeric and between 3-20 characters.
     *
     * @param username the username to validate
     * @return true if valid
     * @throws IllegalArgumentException if username is null
     */
    public boolean validateUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        if (username.length() < 3 || username.length() > 20) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(username).matches();
    }

    /**
     * Sanitizes user input by escaping HTML special characters to prevent XSS.
     *
     * @param input the raw user input
     * @return sanitized string safe for HTML output
     */
    public String sanitizeHtmlInput(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Detects potential SQL injection patterns in user input.
     *
     * @param input the user input to check
     * @return true if suspicious patterns are detected
     */
    public boolean detectSqlInjection(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Detects potential XSS attack patterns in user input.
     *
     * @param input the user input to check
     * @return true if XSS patterns are detected
     */
    public boolean detectXssAttempt(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }

    // ---- Defensive Copying ----

    /**
     * Demonstrates defensive copying to prevent external mutation of internal state.
     */
    public static class ImmutableConfig {
        private final List<String> allowedOrigins;
        private final String applicationName;

        public ImmutableConfig(String applicationName, List<String> allowedOrigins) {
            this.applicationName = Objects.requireNonNull(applicationName, "Application name must not be null");
            // Defensive copy to prevent external modification
            this.allowedOrigins = List.copyOf(
                    Objects.requireNonNull(allowedOrigins, "Allowed origins must not be null")
            );
        }

        public List<String> getAllowedOrigins() {
            // Return unmodifiable list - already immutable via List.copyOf
            return allowedOrigins;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public boolean isOriginAllowed(String origin) {
            return allowedOrigins.contains(origin);
        }
    }

    // ---- Path Traversal Prevention ----

    /**
     * Validates a file path to prevent path traversal attacks.
     *
     * @param basePath     the allowed base directory
     * @param userSupplied the user-supplied path component
     * @return the resolved safe path
     * @throws SecurityException if a path traversal is detected
     */
    public Path validatePath(Path basePath, String userSupplied) {
        Objects.requireNonNull(basePath, "Base path must not be null");
        Objects.requireNonNull(userSupplied, "User supplied path must not be null");

        Path resolved = basePath.resolve(userSupplied).normalize();
        if (!resolved.startsWith(basePath.normalize())) {
            throw new SecurityException("Path traversal detected: " + userSupplied);
        }
        return resolved;
    }

    // ---- Secure Resource Handling ----

    /**
     * Demonstrates proper resource handling with try-with-resources.
     * Returns file content safely, limiting maximum bytes read.
     *
     * @param filePath the path to read
     * @param maxBytes maximum bytes to read
     * @return the file content as string, truncated if necessary
     * @throws IOException       if I/O error occurs
     * @throws SecurityException if file exceeds max size
     */
    public String readFileSafely(Path filePath, long maxBytes) throws IOException {
        Objects.requireNonNull(filePath, "File path must not be null");
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("maxBytes must be positive");
        }

        long fileSize = Files.size(filePath);
        if (fileSize > maxBytes) {
            throw new SecurityException(
                    "File size %d exceeds maximum allowed %d bytes".formatted(fileSize, maxBytes));
        }
        return Files.readString(filePath);
    }

    // ---- Sensitive Data Protection ----

    /**
     * Masks a credit card number showing only the last 4 digits.
     *
     * @param cardNumber the full credit card number
     * @return masked card number
     */
    public String maskCreditCard(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new IllegalArgumentException("Card number must not be null or blank");
        }
        String digitsOnly = cardNumber.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 4) {
            throw new IllegalArgumentException("Card number must have at least 4 digits");
        }
        String lastFour = digitsOnly.substring(digitsOnly.length() - 4);
        return "****-****-****-" + lastFour;
    }

    /**
     * Masks an email address for display purposes.
     * Example: john.doe@example.com -> j*****e@example.com
     *
     * @param email the email to mask
     * @return masked email
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        String[] parts = email.split("@", 2);
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "*@" + domain;
        }
        return localPart.charAt(0)
                + "*".repeat(localPart.length() - 2)
                + localPart.charAt(localPart.length() - 1)
                + "@" + domain;
    }

    // ---- Integer Overflow Prevention ----

    /**
     * Safely adds two integers, throwing on overflow instead of wrapping.
     *
     * @param a first operand
     * @param b second operand
     * @return the sum
     * @throws ArithmeticException if overflow occurs
     */
    public int safeAdd(int a, int b) {
        return Math.addExact(a, b);
    }

    /**
     * Safely multiplies two integers, throwing on overflow.
     *
     * @param a first operand
     * @param b second operand
     * @return the product
     * @throws ArithmeticException if overflow occurs
     */
    public int safeMultiply(int a, int b) {
        return Math.multiplyExact(a, b);
    }
}
