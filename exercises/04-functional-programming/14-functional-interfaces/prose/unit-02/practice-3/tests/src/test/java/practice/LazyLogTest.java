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
        log.debug(() -> "a");

        assertThat(log.lines()).containsExactly("a", "b1", "a");
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

    @Test
    void linesIsASnapshot() {
        LazyLog log = new LazyLog(() -> true);
        log.debug(() -> "first");
        java.util.List<String> before = log.lines();

        log.debug(() -> "second");

        assertThat(before).containsExactly("first");
        assertThat(log.lines()).containsExactly("first", "second");
    }

    @Test
    void eachMessageIsBuiltOnceAndKeptAsIs() {
        int[] built = {0};
        Supplier<String> nothing = () -> {
            built[0]++;
            return null;
        };
        LazyLog log = new LazyLog(() -> true);
        log.debug(nothing);
        log.debug(() -> "after");

        assertThat(built[0]).isEqualTo(1);
        assertThat(log.lines()).containsExactly(null, "after");
    }
}
