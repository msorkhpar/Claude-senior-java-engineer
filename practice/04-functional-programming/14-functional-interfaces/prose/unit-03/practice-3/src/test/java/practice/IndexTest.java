package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IndexTest {

    @Test
    void mapsEachKeyToItsItem() {
        assertThat(Index.indexBy(List.of("Alice", "Bob"), String::toLowerCase))
                .isEqualTo(Map.of("alice", "Alice", "bob", "Bob"));
        assertThat(Index.indexBy(List.<String>of(), String::length)).isEmpty();

        List<String> shared = new ArrayList<>(List.of("x"));
        Map<Integer, List<String>> bySize = Index.indexBy(List.of(shared), List::size);
        assertThat(bySize.get(1)).isSameAs(shared);
    }

    @Test
    void theFirstItemWithAKeyWins() {
        assertThat(Index.indexBy(List.of("Ann", "Amy", "Bob"), s -> s.charAt(0)))
                .isEqualTo(Map.of('A', "Ann", 'B', "Bob"));
    }

    @Test
    void keysKeepTheListsOrder() {
        Map<String, String> index = Index.indexBy(List.of("bob", "al", "cy"), s -> s.substring(0, 1));

        assertThat(index.keySet()).containsExactly("b", "a", "c");
    }

    @Test
    void equalKeysBuiltApartAreOneKey() {
        Map<String, String> index = Index.indexBy(List.of("Ann", "amy", "Bob"),
                s -> new String(s.substring(0, 1).toLowerCase()));

        assertThat(index).hasSize(2);
        assertThat(index.get(new String("a"))).isEqualTo("Ann");
        assertThat(index.get(new String("b"))).isEqualTo("Bob");
    }

    @Test
    void aRepeatedKeyKeepsItsFirstPosition() {
        Map<String, String> index = Index.indexBy(List.of("bob", "al", "bea"), s -> s.substring(0, 1));

        assertThat(index.keySet()).containsExactly("b", "a");
        assertThat(index.get("b")).isEqualTo("bob");
    }
}
