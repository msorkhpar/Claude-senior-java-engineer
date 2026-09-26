package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class LengthIndexTest {

    @Test
    void indexesDistinctLengths() {
        Map<Integer, String> index = LengthIndex.byLength(List.of("apple", "avocado", "blueberry"));
        assertThat(index).containsExactly(entry(5, "apple"), entry(7, "avocado"), entry(9, "blueberry"));
        assertThat(LengthIndex.byLength(List.of())).isEmpty();
    }

    @Test
    void sharedLengthsAreJoined() {
        Map<Integer, String> index = LengthIndex.byLength(List.of("apple", "banana", "cherry", "avocado", "blueberry"));
        assertThat(index).containsExactly(
                entry(5, "apple"), entry(6, "banana, cherry"), entry(7, "avocado"), entry(9, "blueberry"));
    }

    @Test
    void keysKeepFirstSeenOrder() {
        Map<Integer, String> index = LengthIndex.byLength(List.of("blueberry", "apple", "avocado"));
        assertThat(index).containsExactly(entry(9, "blueberry"), entry(5, "apple"), entry(7, "avocado"));
    }

    @Test
    void mergedWordsKeepListOrder() {
        Map<Integer, String> index = LengthIndex.byLength(List.of("fig", "cherry", "kiwi", "banana", "damson"));
        assertThat(index).containsExactly(entry(3, "fig"), entry(6, "cherry, banana, damson"), entry(4, "kiwi"));
    }

    @Test
    void longLengthsStillMerge() {
        String first = "a".repeat(200);
        String second = "b".repeat(200);
        Map<Integer, String> index = LengthIndex.byLength(List.of("fig", first, second));
        assertThat(index).containsExactly(entry(3, "fig"), entry(200, first + ", " + second));
    }
}
