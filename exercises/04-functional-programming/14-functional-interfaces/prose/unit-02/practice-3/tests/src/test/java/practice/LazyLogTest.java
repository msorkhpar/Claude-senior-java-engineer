package practice;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class LazyLogTest {

    @Test
    void recordsMessagesWhileOn() {
        LazyLog log = new LazyLog(() -> true);
        log.debug(() -> "a");
        log.debug(() -> "b" + 1);

        assertThat(log.lines()).containsExactly("a", "b1");
    }

    @Test
    void anOffLogNeverBuildsTheMessage() {
        int[] built = {0};
        Supplier<String> expensive = () -> {
            built[0]++;
            return "expensive";
        };
        LazyLog log = new LazyLog(() -> false);
        log.debug(expensive);
        log.debug(expensive);

        assertThat(log.lines()).isEmpty();
        assertThat(built[0]).isZero();
    }

    @Test
    void theSwitchIsReadOnEveryCall() {
        boolean[] on = {false};
        LazyLog log = new LazyLog(() -> on[0]);
        log.debug(() -> "while off");
        on[0] = true;
        log.debug(() -> "while on");
        on[0] = false;
        log.debug(() -> "off again");

        assertThat(log.lines()).containsExactly("while on");
    }
}
