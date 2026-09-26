package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SafeCollectTest {

    @Test
    void collectsASequentialStream() {
        assertThat(SafeCollect.squares(Stream.of(3, 1, 2))).containsExactly(9L, 1L, 4L);
        assertThat(SafeCollect.squares(Stream.of(100_000))).containsExactly(10_000_000_000L);
        assertThat(SafeCollect.squares(Stream.empty())).isEmpty();
        assertThat(SafeCollect.byFirstLetter(Stream.of("apple", "banana", "avocado")))
                .isEqualTo(Map.of('a', List.of("apple", "avocado"), 'b', List.of("banana")));
    }

    @Test
    void squaresKeepOrderInParallel() {
        List<Integer> numbers = IntStream.range(0, 100_000).boxed().toList();
        List<Long> expected = IntStream.range(0, 100_000).mapToObj(n -> (long) n * n).toList();
        assertThat(SafeCollect.squares(numbers.parallelStream())).isEqualTo(expected);
    }

    @Test
    void groupsKeepOrderInParallel() {
        List<String> words = IntStream.range(0, 100_000).mapToObj(i -> (i % 2 == 0 ? "a" : "b") + i).toList();
        Map<Character, List<String>> groups = SafeCollect.byFirstLetter(words.parallelStream());
        assertThat(groups.get('a')).isEqualTo(words.stream().filter(w -> w.startsWith("a")).toList());
        assertThat(groups.get('b')).isEqualTo(words.stream().filter(w -> w.startsWith("b")).toList());
    }

    @Test
    void lettersGroupByValue() {
        String first = "\u00e9cole";
        String second = "\u00e9t\u00e9";
        String third = "zeal";
        assertThat(SafeCollect.byFirstLetter(Stream.of(first, second, third)))
                .isEqualTo(Map.of('\u00e9', List.of(first, second), 'z', List.of(third)));
    }
}
