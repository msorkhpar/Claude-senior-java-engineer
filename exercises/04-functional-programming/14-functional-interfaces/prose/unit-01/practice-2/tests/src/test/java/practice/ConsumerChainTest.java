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
        IllegalStateException failure = new IllegalStateException("boom");
        Consumer<String> boom = s -> {
            throw failure;
        };
        Consumer<String> c = s -> log.add("c:" + s);
        Consumer<String> chain = ConsumerChain.chain(List.of(a, boom, c));

        assertThatThrownBy(() -> chain.accept("x")).isSameAs(failure);
        assertThat(log).containsExactly("a:x");
    }

    @Test
    void laterListChangesDoNotChangeTheChain() {
        List<String> log = new ArrayList<>();
        Consumer<String> a = s -> log.add("a:" + s);
        Consumer<String> b = s -> log.add("b:" + s);
        List<Consumer<String>> steps = new ArrayList<>(List.of(a));
        Consumer<String> chain = ConsumerChain.chain(steps);

        steps.add(b);
        steps.set(0, b);
        chain.accept("x");

        assertThat(log).containsExactly("a:x");
    }
}
