package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordLoginTest {

    private static final PasswordLogin.Authenticator ACCEPTS_SECRET =
            pw -> Arrays.equals(pw, new char[] {'s', 'e', 'c', 'r', 'e', 't'});

    private static char[] typed(String text) {
        return new StringBuilder(text).toString().toCharArray();
    }

    private static char[] zeros(int n) {
        return new char[n];
    }

    @Test
    void checksThePasswordAndLogsOnlyTheUser() {
        List<String> log = new ArrayList<>();
        List<String> seen = new ArrayList<>();

        boolean ok = PasswordLogin.login("alice", typed("secret"), pw -> {
            seen.add(new String(pw));
            return ACCEPTS_SECRET.authenticate(pw);
        }, log);

        assertThat(ok).isTrue();
        assertThat(seen).containsExactly("secret");
        assertThat(log).containsExactly("Login attempt for user alice");
    }

    @Test
    void theCallersArrayIsClearedAfterASuccess() {
        char[] password = typed("secret");

        PasswordLogin.login("alice", password, ACCEPTS_SECRET, new ArrayList<>());

        assertThat(password).isEqualTo(zeros(6));
    }

    @Test
    void theArrayIsClearedAfterAFailedLogin() {
        char[] password = typed("guess1");
        List<String> log = new ArrayList<>();

        boolean ok = PasswordLogin.login("mallory", password, ACCEPTS_SECRET, log);

        assertThat(ok).isFalse();
        assertThat(password).isEqualTo(zeros(6));
        assertThat(log).containsExactly("Login attempt for user mallory");
    }

    @Test
    void theArrayIsClearedWhenTheCheckThrows() {
        char[] password = typed("secret");

        assertThatThrownBy(() -> PasswordLogin.login("alice", password, pw -> {
            throw new IllegalStateException("user store down");
        }, new ArrayList<>())).isInstanceOf(IllegalStateException.class).hasMessage("user store down");

        assertThat(password).isEqualTo(zeros(6));
    }
}
