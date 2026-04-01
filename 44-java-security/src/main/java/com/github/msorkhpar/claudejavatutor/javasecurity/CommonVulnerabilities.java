package com.github.msorkhpar.claudejavatutor.javasecurity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates common security vulnerabilities in Java and their mitigations.
 * Covers injection flaws, authentication/access control flaws, and sensitive data exposure.
 */
public class CommonVulnerabilities {

    // ---- SQL Injection ----

    /**
     * VULNERABLE: Builds SQL using string concatenation - DO NOT USE in production.
     * This is shown purely for educational purposes.
     */
    public String buildVulnerableQuery(String username) {
        // INSECURE - vulnerable to SQL injection
        return "SELECT * FROM users WHERE username = '" + username + "'";
    }

    /**
     * SECURE: Uses parameterized queries to prevent SQL injection.
     *
     * @param connection the database connection
     * @param username   the username to query
     * @return list of matching usernames
     * @throws SQLException if a database error occurs
     */
    public List<String> findUserSecure(Connection connection, String username) throws SQLException {
        Objects.requireNonNull(connection, "Connection must not be null");
        Objects.requireNonNull(username, "Username must not be null");

        String sql = "SELECT username FROM users WHERE username = ?";
        List<String> results = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("username"));
                }
            }
        }
        return results;
    }

    // ---- XSS Prevention ----

    /**
     * VULNERABLE: Returns user input directly without encoding.
     */
    public String renderVulnerableHtml(String userInput) {
        return "<div>" + userInput + "</div>";
    }

    /**
     * SECURE: Encodes user input before rendering in HTML.
     */
    public String renderSafeHtml(String userInput) {
        if (userInput == null) {
            return "<div></div>";
        }
        String escaped = userInput
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
        return "<div>" + escaped + "</div>";
    }

    // ---- Brute Force Protection ----

    /**
     * Simple rate limiter / brute-force protector for login attempts.
     */
    public static class LoginAttemptTracker {
        private static final int MAX_ATTEMPTS = 5;
        private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000L; // 15 minutes

        private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

        record AttemptInfo(AtomicInteger count, long firstAttemptTime, long lockoutUntil) {
            AttemptInfo(long currentTime) {
                this(new AtomicInteger(1), currentTime, 0L);
            }
        }

        /**
         * Records a failed login attempt and returns whether the account is now locked.
         *
         * @param username the username that failed login
         * @return true if the account is now locked out
         */
        public boolean recordFailedAttempt(String username) {
            Objects.requireNonNull(username, "Username must not be null");
            long now = System.currentTimeMillis();

            AttemptInfo info = attempts.get(username);
            if (info == null) {
                attempts.put(username, new AttemptInfo(now));
                return false;
            }

            if (info.lockoutUntil > 0 && now < info.lockoutUntil) {
                return true; // still locked
            }

            if (info.lockoutUntil > 0 && now >= info.lockoutUntil) {
                // lockout expired, reset
                attempts.put(username, new AttemptInfo(now));
                return false;
            }

            int currentCount = info.count.incrementAndGet();
            if (currentCount >= MAX_ATTEMPTS) {
                attempts.put(username, new AttemptInfo(
                        new AtomicInteger(currentCount),
                        info.firstAttemptTime,
                        now + LOCKOUT_DURATION_MS
                ));
                return true;
            }
            return false;
        }

        /**
         * Checks whether the given user is currently locked out.
         */
        public boolean isLockedOut(String username) {
            AttemptInfo info = attempts.get(username);
            if (info == null) {
                return false;
            }
            if (info.lockoutUntil > 0 && System.currentTimeMillis() < info.lockoutUntil) {
                return true;
            }
            return false;
        }

        /**
         * Resets attempts after a successful login.
         */
        public void resetAttempts(String username) {
            attempts.remove(username);
        }

        /**
         * Returns the number of recorded failed attempts.
         */
        public int getFailedAttemptCount(String username) {
            AttemptInfo info = attempts.get(username);
            return info == null ? 0 : info.count.get();
        }
    }

    // ---- Access Control ----

    /**
     * Demonstrates role-based access control (RBAC).
     */
    public static class AccessControl {

        public enum Role {
            GUEST, USER, ADMIN, SUPER_ADMIN
        }

        public enum Permission {
            READ, WRITE, DELETE, MANAGE_USERS
        }

        private static final Map<Role, List<Permission>> ROLE_PERMISSIONS = Map.of(
                Role.GUEST, List.of(Permission.READ),
                Role.USER, List.of(Permission.READ, Permission.WRITE),
                Role.ADMIN, List.of(Permission.READ, Permission.WRITE, Permission.DELETE),
                Role.SUPER_ADMIN, Arrays.asList(Permission.values())
        );

        /**
         * Checks if a given role has the specified permission.
         *
         * @param role       the user's role
         * @param permission the required permission
         * @return true if the role grants the permission
         */
        public boolean hasPermission(Role role, Permission permission) {
            Objects.requireNonNull(role, "Role must not be null");
            Objects.requireNonNull(permission, "Permission must not be null");

            List<Permission> permissions = ROLE_PERMISSIONS.get(role);
            return permissions != null && permissions.contains(permission);
        }

        /**
         * Enforces a permission check and throws if unauthorized.
         *
         * @param role       the user's role
         * @param permission the required permission
         * @throws SecurityException if the role lacks the permission
         */
        public void enforcePermission(Role role, Permission permission) {
            if (!hasPermission(role, permission)) {
                throw new SecurityException(
                        "Role %s does not have permission %s".formatted(role, permission));
            }
        }

        /**
         * Returns all permissions for a given role.
         */
        public List<Permission> getPermissions(Role role) {
            Objects.requireNonNull(role, "Role must not be null");
            List<Permission> perms = ROLE_PERMISSIONS.get(role);
            return perms != null ? List.copyOf(perms) : List.of();
        }
    }

    // ---- Sensitive Data Exposure ----

    /**
     * Demonstrates INSECURE storage of sensitive data.
     * Passwords should NEVER be stored in plain text.
     */
    public static class InsecureDataStore {
        private final Map<String, String> passwords = new HashMap<>();

        public void storePassword(String user, String plainPassword) {
            // INSECURE - storing plain text passwords
            passwords.put(user, plainPassword);
        }

        public boolean verifyPassword(String user, String password) {
            return password.equals(passwords.get(user));
        }
    }

    /**
     * Demonstrates safe error messages that do not leak internal details.
     */
    public static class SafeErrorHandler {

        /**
         * Returns a generic error message for login failures -
         * never reveals whether the username or password was wrong.
         */
        public String handleLoginFailure(String username, boolean userExists) {
            // SECURE: same message regardless of whether user exists
            return "Invalid username or password";
        }

        /**
         * Returns a sanitized error message that does not expose stack traces or internals.
         */
        public String handleException(Exception e) {
            // Log the real exception internally (in production, use a logging framework)
            // Return a generic message to the client
            return "An internal error occurred. Please try again later.";
        }

        /**
         * Demonstrates timing-safe string comparison to prevent timing attacks.
         * Uses constant-time comparison regardless of where strings differ.
         */
        public boolean constantTimeEquals(String a, String b) {
            if (a == null || b == null) {
                return a == b;
            }
            if (a.length() != b.length()) {
                return false;
            }
            int result = 0;
            for (int i = 0; i < a.length(); i++) {
                result |= a.charAt(i) ^ b.charAt(i);
            }
            return result == 0;
        }
    }

    // ---- Insecure Deserialization Prevention ----

    /**
     * Demonstrates safe data transfer using records instead of Java serialization.
     * Java serialization is inherently risky and should be avoided.
     */
    public record SafeUserDTO(String username, String email, String role) {

        public SafeUserDTO {
            Objects.requireNonNull(username, "Username must not be null");
            Objects.requireNonNull(email, "Email must not be null");
            Objects.requireNonNull(role, "Role must not be null");
            if (username.isBlank()) {
                throw new IllegalArgumentException("Username must not be blank");
            }
        }

        /**
         * Creates a DTO from a map of values (simulating deserialization from JSON).
         */
        public static SafeUserDTO fromMap(Map<String, String> data) {
            return new SafeUserDTO(
                    data.getOrDefault("username", ""),
                    data.getOrDefault("email", ""),
                    data.getOrDefault("role", "GUEST")
            );
        }
    }
}
