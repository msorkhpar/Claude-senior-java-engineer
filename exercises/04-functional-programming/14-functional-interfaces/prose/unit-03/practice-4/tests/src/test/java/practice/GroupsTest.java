package practice;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class GroupsTest {

    @Test
    void groupsWordsByFirstLetter() {
        Map<Character, List<String>> groups =
                Groups.byFirstLetter(List.of("apple", "banana", "avocado", "cherry", "blueberry"));

        assertThat(groups).isEqualTo(Map.of(
                'a', List.of("apple", "avocado"),
                'b', List.of("banana", "blueberry"),
                'c', List.of("cherry")));
        assertThat(Groups.byFirstLetter(List.of())).isEmpty();
    }

    @Test
    void cachesAMeasuredValue() {
        Map<String, Integer> cache = new HashMap<>();

        assertThat(Groups.lengthOf(cache, "hello", String::length)).isEqualTo(5);
        assertThat(cache).containsEntry("hello", 5);
    }

    @Test
    void theMeasureRunsOncePerWord() {
        int[] calls = {0};
        Function<String, Integer> measure = s -> {
            calls[0]++;
            return s.length();
        };
        Map<String, Integer> cache = new HashMap<>();

        assertThat(Groups.lengthOf(cache, "hello", measure)).isEqualTo(5);
        assertThat(Groups.lengthOf(cache, "hello", measure)).isEqualTo(5);
        assertThat(Groups.lengthOf(cache, "hi", measure)).isEqualTo(2);
        assertThat(calls[0]).isEqualTo(2);
    }

    @Test
    void groupingIgnoresCase() {
        assertThat(Groups.byFirstLetter(List.of("Apple", "avocado", "Banana")))
                .isEqualTo(Map.of('a', List.of("Apple", "avocado"), 'b', List.of("Banana")));
    }

    @Test
    void emptyWordsAreSkipped() {
        assertThat(Groups.byFirstLetter(List.of("", "kiwi", "")))
                .isEqualTo(Map.of('k', List.of("kiwi")));
    }

    @Test
    void anEqualWordBuiltElsewhereHitsTheCache() {
        int[] calls = {0};
        Function<String, Integer> measure = s -> {
            calls[0]++;
            return s.length();
        };
        Map<String, Integer> cache = new HashMap<>();

        assertThat(Groups.lengthOf(cache, new String("hello"), measure)).isEqualTo(5);
        assertThat(Groups.lengthOf(cache, new StringBuilder("hel").append("lo").toString(), measure)).isEqualTo(5);
        assertThat(calls[0]).isEqualTo(1);
    }

    @Test
    void onlyEmptyWordsAreSkipped() {
        assertThat(Groups.byFirstLetter(List.of(" kiwi", "  ", "lime")))
                .isEqualTo(Map.of(' ', List.of(" kiwi", "  "), 'l', List.of("lime")));
    }

    @Test
    void theCacheKeyIsTheWordAsGiven() {
        int[] calls = {0};
        Function<String, Integer> measure = s -> {
            calls[0]++;
            return s.length();
        };
        Map<String, Integer> cache = new HashMap<>();

        Groups.lengthOf(cache, "Hello", measure);
        Groups.lengthOf(cache, "hello", measure);

        assertThat(cache).containsOnlyKeys("Hello", "hello");
        assertThat(calls[0]).isEqualTo(2);
    }
}
