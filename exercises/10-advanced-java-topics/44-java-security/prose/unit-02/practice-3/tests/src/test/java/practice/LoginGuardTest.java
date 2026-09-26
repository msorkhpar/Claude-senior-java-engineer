package practice;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginGuardTest {

    static final class TestClock extends Clock {
        private Instant now = Instant.parse("2024-03-01T09:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private static final Map<String, String> USERS = Map.of("alice", "s3cret", "bob", "hunter22");

    private static final LoginGuard.Credentials CREDENTIALS = new LoginGuard.Credentials() {
        @Override
        public boolean exists(String user) {
            return USERS.containsKey(user);
        }

        @Override
        public boolean matches(String user, String password) {
            return password.equals(USERS.get(user));
        }
    };

    private final TestClock clock = new TestClock();
    private final LoginGuard guard = new LoginGuard(CREDENTIALS, clock);

    private void fail(String user, int times) {
        for (int i = 0; i < times; i++) {
            assertThat(guard.login(user, "wrong" + i)).isEqualTo(LoginGuard.GENERIC);
        }
    }

    @Test
    void locksAfterFiveFailuresInARow() {
        assertThat(guard.login("alice", "s3cret")).isEqualTo("Welcome, alice");
        assertThat(guard.login("alice", "wrong")).isEqualTo("Invalid username or password");
        clock.advance(Duration.ofMinutes(1));
        fail("alice", 4);

        clock.advance(Duration.ofMinutes(9));
        assertThatThrownBy(() -> guard.login("alice", "wrong"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Account temporarily locked");
        assertThat(guard.login("bob", "hunter22")).isEqualTo("Welcome, bob");
    }

    @Test
    void anUnknownUserGetsTheSameMessage() {
        assertThat(guard.login("ghost", "x")).isEqualTo(LoginGuard.GENERIC);
        assertThat(guard.login("alice", "x")).isEqualTo(LoginGuard.GENERIC);
    }

    @Test
    void aLockedAccountRefusesTheRightPassword() {
        fail("alice", 5);

        assertThatThrownBy(() -> guard.login("alice", "s3cret"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Account temporarily locked");
    }

    @Test
    void theLockEndsFifteenMinutesAfterTheFifthFailure() {
        fail("alice", 5);

        clock.advance(Duration.ofMinutes(15).minusSeconds(1));
        assertThatThrownBy(() -> guard.login("alice", "s3cret")).isInstanceOf(SecurityException.class);
        clock.advance(Duration.ofSeconds(1));
        assertThat(guard.login("alice", "wrong")).isEqualTo(LoginGuard.GENERIC);
        assertThat(guard.login("alice", "s3cret")).isEqualTo("Welcome, alice");
    }

    @Test
    void aSuccessResetsTheCount() {
        fail("alice", 4);
        assertThat(guard.login("alice", "s3cret")).isEqualTo("Welcome, alice");
        fail("alice", 4);

        assertThat(guard.login("alice", "s3cret")).isEqualTo("Welcome, alice");
    }

    @Test
    void capitalisationDoesNotDodgeTheLock() {
        guard.login("alice", "w1");
        guard.login("Alice", "w2");
        guard.login("ALICE", "w3");
        guard.login("aLiCe", "w4");
        guard.login("alicE", "w5");

        assertThatThrownBy(() -> guard.login("alice", "s3cret")).isInstanceOf(SecurityException.class);
        assertThat(new LoginGuard(CREDENTIALS, clock).login("ALICE", "s3cret")).isEqualTo("Welcome, alice");
    }
}
