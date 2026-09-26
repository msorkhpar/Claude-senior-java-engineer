package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class ClosuresTest {

    @Test
    void multiplierMultiplies() {
        Function<Integer, Integer> triple = Closures.multiplier(3);
        assertThat(triple.apply(5)).isEqualTo(15);
        assertThat(triple.apply(10)).isEqualTo(30);
        assertThat(triple.apply(0)).isEqualTo(0);
    }

    @Test
    void betweenChecksTheRange() {
        Predicate<Integer> inRange = Closures.between(10, 20);
        assertThat(inRange.test(15)).isTrue();
        assertThat(inRange.test(5)).isFalse();
        assertThat(inRange.test(25)).isFalse();
    }

    @Test
    void prefixFilterKeepsMatches() {
        Function<List<String>, List<String>> filter = Closures.prefixFilter("hel", 5);
        assertThat(filter.apply(List.of("hello", "help", "hi", "helicopter")))
                .containsExactly("hello", "helicopter");
        assertThat(filter.apply(List.of())).isEmpty();
    }

    @Test
    void boundsAreInclusive() {
        Predicate<Integer> inRange = Closures.between(10, 20);
        assertThat(inRange.test(10)).isTrue();
        assertThat(inRange.test(20)).isTrue();
        assertThat(inRange.test(9)).isFalse();
        assertThat(inRange.test(21)).isFalse();
    }

    @Test
    void eachClosureKeepsItsOwnValue() {
        Function<Integer, Integer> doubler = Closures.multiplier(2);
        Function<Integer, Integer> tripler = Closures.multiplier(3);
        assertThat(doubler.apply(5)).isEqualTo(10);
        assertThat(tripler.apply(5)).isEqualTo(15);
    }
}
