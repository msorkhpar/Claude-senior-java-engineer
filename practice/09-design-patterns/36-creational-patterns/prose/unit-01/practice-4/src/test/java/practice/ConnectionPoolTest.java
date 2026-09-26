package practice;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConnectionPoolTest {

    private final ConnectionPool pool = ConnectionPool.valueOf("INSTANCE");

    @BeforeEach
    void startEmpty() {
        try {
            pool.clear();
        } catch (UnsupportedOperationException notWrittenYet) {
            // the tests below fail on their own
        }
    }

    @Test
    void keepsConnectionsInOrder() {
        pool.addConnection("jdbc:h2:mem:a");
        ConnectionPool.INSTANCE.addConnection("jdbc:h2:mem:b");

        assertThat(pool.connections()).containsExactly("jdbc:h2:mem:a", "jdbc:h2:mem:b");
    }

    @Test
    void callersCannotChangeTheList() {
        pool.addConnection("jdbc:h2:mem:a");
        List<String> seen = pool.connections();

        assertThatThrownBy(() -> seen.add("jdbc:h2:mem:x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> seen.set(0, "jdbc:h2:mem:x")).isInstanceOf(UnsupportedOperationException.class);
        assertThat(pool.connections()).containsExactly("jdbc:h2:mem:a");
    }

    @Test
    void refusesABlankConnection() {
        pool.addConnection("jdbc:h2:mem:a");

        assertThatThrownBy(() -> pool.addConnection("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> pool.addConnection(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(pool.connections()).containsExactly("jdbc:h2:mem:a");
    }
}
