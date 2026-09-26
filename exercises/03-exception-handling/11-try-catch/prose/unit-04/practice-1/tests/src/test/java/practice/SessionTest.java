package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionTest {

    @Test
    void closesAtTheEndOfTheTry() {
        List<String> log = new ArrayList<>();
        Session kept;
        try (Session s = new Session("db", log)) {
            s.use("query");
            assertThat(s.isClosed()).isFalse();
            kept = s;
        }
        assertThat(kept.isClosed()).isTrue();
        assertThat(log).containsExactly("use query", "close db");

        List<String> shared = new ArrayList<>();
        Session first = new Session("db", shared);
        Session second = new Session("db", shared);
        try (first) {
            first.use("a");
        }
        assertThat(first.isClosed()).isTrue();
        assertThat(second.isClosed()).isFalse();
        second.use("b");
        second.close();
        assertThat(shared).containsExactly("use a", "close db", "use b", "close db");
    }

    @Test
    void closesWhenTheBodyThrows() {
        List<String> log = new ArrayList<>();
        Session s = new Session("db", log);
        assertThatThrownBy(() -> {
            try (s) {
                s.use("query");
                throw new IllegalArgumentException("bad query");
            }
        }).isInstanceOf(IllegalArgumentException.class).hasMessage("bad query");
        assertThat(s.isClosed()).isTrue();
        assertThat(log).containsExactly("use query", "close db");
    }

    @Test
    void aClosedSessionRefusesUse() {
        List<String> log = new ArrayList<>();
        Session s = new Session("db", log);
        try (s) {
            s.use("query");
        }
        assertThatThrownBy(() -> s.use("late"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Resource is already closed");
        assertThat(log).containsExactly("use query", "close db");
    }

    @Test
    void closingTwiceClosesOnce() {
        List<String> log = new ArrayList<>();
        Session s = new Session("db", log);
        s.close();
        s.close();
        assertThat(s.isClosed()).isTrue();
        assertThat(log).containsExactly("close db");
    }
}
