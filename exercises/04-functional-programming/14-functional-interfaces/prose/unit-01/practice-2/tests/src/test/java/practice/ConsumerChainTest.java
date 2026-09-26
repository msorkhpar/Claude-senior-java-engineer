package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsumerChainTest {

    @Test
    void runsStepsLeftToRightOnTheSameInput() {
        List<String> log = new ArrayList<>();
        Consumer<String> logger = s -> log.add("log:" + s);
        Consumer<String> storer = s -> log.add("store:" + s);
        Consumer<String> shouter = s -> log.add("shout:" + s.toUpperCase());

        ConsumerChain.chain(List.of(logger, storer, shouter)).accept("Alice");
        assertThat(log).containsExactly("log:Alice", "store:Alice", "shout:ALICE");

        log.clear();
        ConsumerChain.chain(List.of(storer, logger)).accept("Bob");
        assertThat(log).containsExactly("store:Bob", "log:Bob");

        log.clear();
        ConsumerChain.chain(List.of(logger, logger)).accept("x");
        assertThat(log).containsExactly("log:x", "log:x");
    }

    @Test
    void nullStepsAreSkipped() {
        List<String> log = new ArrayList<>();
        Consumer<String> a = s -> log.add("a:" + s);
        Consumer<String> b = s -> log.add("b:" + s);

        ConsumerChain.chain(Arrays.asList(a, null, b, null)).accept("x");

        assertThat(log).containsExactly("a:x", "b:x");
    }

    @Test
    void anEmptyChainDoesNothing() {
        Consumer<String> nothing = ConsumerChain.chain(List.of());

        assertThat(nothing).isNotNull();
        assertThatCode(() -> nothing.accept("x")).doesNotThrowAnyException();
    }

    @Test
    void aThrowingStepStopsTheRest() {
        List<String> log = new ArrayList<>();
        Consumer<String> a = s -> log.add("a:" + s);
        Consumer<String> boom = s -> {
            throw new IllegalStateException("boom");
        };
        Consumer<String> c = s -> log.add("c:" + s);
        Consumer<String> chain = ConsumerChain.chain(List.of(a, boom, c));

        assertThatThrownBy(() -> chain.accept("x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
        assertThat(log).containsExactly("a:x");
    }
}
