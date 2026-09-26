package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatchCounterTest {

    @Test
    void countsMatchingItems() {
        assertThat(new MatchCounter().countMatches(
                List.of("apple", "banana", "apricot", "cherry"), s -> s.startsWith("a"))).isEqualTo(2);
        assertThat(new MatchCounter().countMatches(List.of(), s -> true)).isZero();
        assertThat(new MatchCounter().countMatches(List.of("apple", "apple", "kiwi"), s -> s.startsWith("a")))
                .isEqualTo(2);
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

    @Test
    void conditionNeverSeesNull() {
        List<String> seen = new ArrayList<>();
        List<String> items = Arrays.asList("apple", null, "avocado");
        int count = new MatchCounter().countMatches(items, s -> {
            seen.add(s);
            return s.startsWith("a");
        });
        assertThat(count).isEqualTo(2);
        assertThat(seen).containsExactly("apple", "avocado");
    }

    @Test
    void nestedCallsDoNotInterfere() {
        MatchCounter counter = new MatchCounter();
        List<Integer> inner = new java.util.ArrayList<>();
        int outer = counter.countMatches(List.of("a1", "a2", "a3"), s -> {
            inner.add(counter.countMatches(List.of("v", "w", "x", "y", "z"), t -> true));
            return true;
        });
        assertThat(outer).isEqualTo(3);
        assertThat(inner).containsExactly(5, 5, 5);
    }
}
