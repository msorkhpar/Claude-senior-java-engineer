package com.github.msorkhpar.claudejavatutor.javasecurity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Common Vulnerabilities Tests")
class CommonVulnerabilitiesTest {

    @Nested
    @DisplayName("SQL Injection")
    class SqlInjectionTest {

        private final CommonVulnerabilities vuln = new CommonVulnerabilities();

        @Test
        @DisplayName("Should show vulnerable query with injected SQL")
        void testVulnerableQueryInjection() {
            String malicious = "admin' OR '1'='1";
            String query = vuln.buildVulnerableQuery(malicious);

            // Demonstrates the vulnerability: the injected SQL becomes part of the query
            assertThat(query)
                    .contains("OR '1'='1")
                    .startsWith("SELECT * FROM users");
        }

        @Test
        @DisplayName("Should show vulnerable query with normal input")
        void testVulnerableQueryNormal() {
            String query = vuln.buildVulnerableQuery("alice");
            assertThat(query).isEqualTo("SELECT * FROM users WHERE username = 'alice'");
        }

        @Test
        @DisplayName("Should show vulnerable query with DROP TABLE")
        void testVulnerableQueryDropTable() {
            String malicious = "'; DROP TABLE users; --";
            String query = vuln.buildVulnerableQuery(malicious);
            assertThat(query).contains("DROP TABLE users");
        }
    }

    @Nested
    @DisplayName("XSS Prevention")
    class XssPreventionTest {

        private final CommonVulnerabilities vuln = new CommonVulnerabilities();

        @Test
        @DisplayName("Vulnerable render should include raw script tags")
        void testVulnerableHtml() {
            String result = vuln.renderVulnerableHtml("<script>alert('xss')</script>");
            assertThat(result).contains("<script>");
        }

        @Test
        @DisplayName("Safe render should escape script tags")
        void testSafeHtml() {
            String result = vuln.renderSafeHtml("<script>alert('xss')</script>");
            assertThat(result)
                    .doesNotContain("<script>")
                    .contains("&lt;script&gt;");
        }

        @Test
        @DisplayName("Safe render should handle null input")
        void testSafeHtmlNull() {
            assertThat(vuln.renderSafeHtml(null)).isEqualTo("<div></div>");
        }

        @Test
        @DisplayName("Safe render should handle normal text")
        void testSafeHtmlNormal() {
            assertThat(vuln.renderSafeHtml("Hello World"))
                    .isEqualTo("<div>Hello World</div>");
        }

        @Test
        @DisplayName("Safe render should escape all special chars")
        void testSafeHtmlAllSpecialChars() {
            String result = vuln.renderSafeHtml("a & b < c > d \"e\" 'f'");
            assertThat(result).contains("&amp;", "&lt;", "&gt;", "&quot;", "&#x27;");
        }
    }

    @Nested
    @DisplayName("Login Attempt Tracker")
    class LoginAttemptTrackerTest {

        @Test
        @DisplayName("Should not lock after fewer than 5 attempts")
        void testNoLockUnder5Attempts() {
            var tracker = new CommonVulnerabilities.LoginAttemptTracker();
            for (int i = 0; i < 4; i++) {
                assertThat(tracker.recordFailedAttempt("user1")).isFalse();
            }
            assertThat(tracker.isLockedOut("user1")).isFalse();
        }

        @Test
        @DisplayName("Should lock after 5 failed attempts")
        void testLockAfter5Attempts() {
            var tracker = new CommonVulnerabilities.LoginAttemptTracker();
            for (int i = 0; i < 4; i++) {
                tracker.recordFailedAttempt("user1");
            }
            boolean locked = tracker.recordFailedAttempt("user1");
            assertThat(locked).isTrue();
            assertThat(tracker.isLockedOut("user1")).isTrue();
        }

        @Test
        @DisplayName("Should reset attempts on successful login")
        void testResetAttempts() {
            var tracker = new CommonVulnerabilities.LoginAttemptTracker();
            tracker.recordFailedAttempt("user1");
            tracker.recordFailedAttempt("user1");
            tracker.resetAttempts("user1");
            assertThat(tracker.getFailedAttemptCount("user1")).isZero();
        }

        @Test
        @DisplayName("Should track different users independently")
        void testDifferentUsers() {
            var tracker = new CommonVulnerabilities.LoginAttemptTracker();
            tracker.recordFailedAttempt("user1");
            tracker.recordFailedAttempt("user2");
            assertThat(tracker.getFailedAttemptCount("user1")).isEqualTo(1);
            assertThat(tracker.getFailedAttemptCount("user2")).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return 0 for unknown user")
        void testUnknownUser() {
            var tracker = new CommonVulnerabilities.LoginAttemptTracker();
            assertThat(tracker.getFailedAttemptCount("unknown")).isZero();
            assertThat(tracker.isLockedOut("unknown")).isFalse();
        }

        @Test
        @DisplayName("Should throw on null username")
        void testNullUsername() {
            var tracker = new CommonVulnerabilities.LoginAttemptTracker();
            assertThatNullPointerException()
                    .isThrownBy(() -> tracker.recordFailedAttempt(null));
        }
    }

    @Nested
    @DisplayName("Access Control")
    class AccessControlTest {

        private final CommonVulnerabilities.AccessControl ac = new CommonVulnerabilities.AccessControl();

        @Test
        @DisplayName("Guest should only have READ permission")
        void testGuestPermissions() {
            assertThat(ac.hasPermission(
                    CommonVulnerabilities.AccessControl.Role.GUEST,
                    CommonVulnerabilities.AccessControl.Permission.READ)).isTrue();
            assertThat(ac.hasPermission(
                    CommonVulnerabilities.AccessControl.Role.GUEST,
                    CommonVulnerabilities.AccessControl.Permission.WRITE)).isFalse();
            assertThat(ac.hasPermission(
                    CommonVulnerabilities.AccessControl.Role.GUEST,
                    CommonVulnerabilities.AccessControl.Permission.DELETE)).isFalse();
        }

        @Test
        @DisplayName("Admin should have READ, WRITE, DELETE permissions")
        void testAdminPermissions() {
            assertThat(ac.hasPermission(
                    CommonVulnerabilities.AccessControl.Role.ADMIN,
                    CommonVulnerabilities.AccessControl.Permission.READ)).isTrue();
            assertThat(ac.hasPermission(
                    CommonVulnerabilities.AccessControl.Role.ADMIN,
                    CommonVulnerabilities.AccessControl.Permission.WRITE)).isTrue();
            assertThat(ac.hasPermission(
                    CommonVulnerabilities.AccessControl.Role.ADMIN,
                    CommonVulnerabilities.AccessControl.Permission.DELETE)).isTrue();
            assertThat(ac.hasPermission(
                    CommonVulnerabilities.AccessControl.Role.ADMIN,
                    CommonVulnerabilities.AccessControl.Permission.MANAGE_USERS)).isFalse();
        }

        @Test
        @DisplayName("Super Admin should have all permissions")
        void testSuperAdminPermissions() {
            for (var permission : CommonVulnerabilities.AccessControl.Permission.values()) {
                assertThat(ac.hasPermission(
                        CommonVulnerabilities.AccessControl.Role.SUPER_ADMIN, permission)).isTrue();
            }
        }

        @Test
        @DisplayName("Should enforce permission and throw SecurityException")
        void testEnforcePermission() {
            assertThatExceptionOfType(SecurityException.class)
                    .isThrownBy(() -> ac.enforcePermission(
                            CommonVulnerabilities.AccessControl.Role.GUEST,
                            CommonVulnerabilities.AccessControl.Permission.DELETE))
                    .withMessageContaining("GUEST")
                    .withMessageContaining("DELETE");
        }

        @Test
        @DisplayName("Should not throw when permission exists")
        void testEnforcePermissionGranted() {
            assertThatNoException()
                    .isThrownBy(() -> ac.enforcePermission(
                            CommonVulnerabilities.AccessControl.Role.ADMIN,
                            CommonVulnerabilities.AccessControl.Permission.READ));
        }

        @Test
        @DisplayName("Should return all permissions for a role")
        void testGetPermissions() {
            List<CommonVulnerabilities.AccessControl.Permission> perms =
                    ac.getPermissions(CommonVulnerabilities.AccessControl.Role.USER);
            assertThat(perms).containsExactlyInAnyOrder(
                    CommonVulnerabilities.AccessControl.Permission.READ,
                    CommonVulnerabilities.AccessControl.Permission.WRITE);
        }

        @Test
        @DisplayName("Should throw on null role")
        void testNullRole() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ac.hasPermission(null,
                            CommonVulnerabilities.AccessControl.Permission.READ));
        }

        @Test
        @DisplayName("Should throw on null permission")
        void testNullPermission() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ac.hasPermission(
                            CommonVulnerabilities.AccessControl.Role.ADMIN, null));
        }
    }

    @Nested
    @DisplayName("Safe Error Handler")
    class SafeErrorHandlerTest {

        private final CommonVulnerabilities.SafeErrorHandler handler =
                new CommonVulnerabilities.SafeErrorHandler();

        @Test
        @DisplayName("Should return generic message regardless of user existence")
        void testLoginFailureMessage() {
            String messageUserExists = handler.handleLoginFailure("admin", true);
            String messageUserNotExists = handler.handleLoginFailure("unknown", false);

            // Both should be identical - no information leakage
            assertThat(messageUserExists).isEqualTo(messageUserNotExists);
            assertThat(messageUserExists).isEqualTo("Invalid username or password");
        }

        @Test
        @DisplayName("Should return generic error message for exceptions")
        void testHandleException() {
            String message = handler.handleException(new RuntimeException("DB connection failed"));
            assertThat(message)
                    .doesNotContain("DB connection")
                    .contains("internal error");
        }

        @Test
        @DisplayName("Should perform constant-time string comparison - equal strings")
        void testConstantTimeEqualsMatch() {
            assertThat(handler.constantTimeEquals("secret", "secret")).isTrue();
        }

        @Test
        @DisplayName("Should perform constant-time string comparison - different strings")
        void testConstantTimeEqualsDiff() {
            assertThat(handler.constantTimeEquals("secret", "secre1")).isFalse();
        }

        @Test
        @DisplayName("Should perform constant-time string comparison - different lengths")
        void testConstantTimeEqualsDiffLength() {
            assertThat(handler.constantTimeEquals("short", "longer")).isFalse();
        }

        @Test
        @DisplayName("Should handle nulls in constant-time comparison")
        void testConstantTimeEqualsNull() {
            assertThat(handler.constantTimeEquals(null, null)).isTrue();
            assertThat(handler.constantTimeEquals(null, "a")).isFalse();
            assertThat(handler.constantTimeEquals("a", null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Safe User DTO")
    class SafeUserDTOTest {

        @Test
        @DisplayName("Should create valid DTO")
        void testValidDTO() {
            var dto = new CommonVulnerabilities.SafeUserDTO("alice", "alice@test.com", "USER");
            assertThat(dto.username()).isEqualTo("alice");
            assertThat(dto.email()).isEqualTo("alice@test.com");
            assertThat(dto.role()).isEqualTo("USER");
        }

        @Test
        @DisplayName("Should throw on null username")
        void testNullUsername() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CommonVulnerabilities.SafeUserDTO(null, "a@b.com", "USER"));
        }

        @Test
        @DisplayName("Should throw on blank username")
        void testBlankUsername() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CommonVulnerabilities.SafeUserDTO("", "a@b.com", "USER"));
        }

        @Test
        @DisplayName("Should throw on null email")
        void testNullEmail() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CommonVulnerabilities.SafeUserDTO("alice", null, "USER"));
        }

        @Test
        @DisplayName("Should throw on null role")
        void testNullRole() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CommonVulnerabilities.SafeUserDTO("alice", "a@b.com", null));
        }

        @Test
        @DisplayName("Should create DTO from map")
        void testFromMap() {
            Map<String, String> data = Map.of(
                    "username", "bob",
                    "email", "bob@test.com",
                    "role", "ADMIN"
            );
            var dto = CommonVulnerabilities.SafeUserDTO.fromMap(data);
            assertThat(dto.username()).isEqualTo("bob");
            assertThat(dto.role()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Should use defaults from map when keys missing")
        void testFromMapDefaults() {
            var dto = CommonVulnerabilities.SafeUserDTO.fromMap(Map.of("username", "test", "email", "t@t.com"));
            assertThat(dto.role()).isEqualTo("GUEST");
        }
    }

    @Nested
    @DisplayName("Insecure Data Store")
    class InsecureDataStoreTest {

        @Test
        @DisplayName("Should demonstrate insecure plain-text password storage")
        void testInsecureStore() {
            var store = new CommonVulnerabilities.InsecureDataStore();
            store.storePassword("user", "password123");
            assertThat(store.verifyPassword("user", "password123")).isTrue();
            assertThat(store.verifyPassword("user", "wrong")).isFalse();
        }

        @Test
        @DisplayName("Should return false for non-existent user")
        void testNonExistentUser() {
            var store = new CommonVulnerabilities.InsecureDataStore();
            // password.equals(null) returns false - no NPE
            assertThat(store.verifyPassword("unknown", "pass")).isFalse();
        }
    }
}
