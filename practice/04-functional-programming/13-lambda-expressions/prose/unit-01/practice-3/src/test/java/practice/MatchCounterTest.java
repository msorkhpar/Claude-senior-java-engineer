package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatchCounterTest {

    @Test
    void countsMatchingItems() {
        assertThat(new MatchCounter().countMatches(
                List.of("apple", "banana", "apricot", "cherry"), s -> s.startsWith("a"))).isEqualTo(2);
        assertThat(new MatchCounter().countMatches(List.of(), s -> true)).isZero();
    }

    @Test
    void nullItemsAreSkipped() {
        List<String> items = Arrays.asList("apple", null, "avocado", null);
        assertThat(new MatchCounter().countMatches(items, s -> s.startsWith("a"))).isEqualTo(2);
        assertThat(new MatchCounter().countMatches(items, s -> true)).isEqualTo(2);
    }

    @Test
    void eachCallStartsFromZero() {
        MatchCounter counter = new MatchCounter();
        List<String> items = List.of("apple", "banana", "apricot");
        assertThat(counter.countMatches(items, s -> s.startsWith("a"))).isEqualTo(2);
        assertThat(counter.countMatches(items, s -> s.startsWith("a"))).isEqualTo(2);
        assertThat(counter.countMatches(items, s -> s.startsWith("b"))).isEqualTo(1);
    }
}
