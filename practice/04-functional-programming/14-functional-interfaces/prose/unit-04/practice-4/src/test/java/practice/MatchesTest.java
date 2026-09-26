package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatchesTest {

    @Test
    void countsAndRemovesATarget() {
        List<String> names = List.of("Alice", "Bob", "Alice", "Charlie");
        assertThat(Matches.countEqual(names, "Alice")).isEqualTo(2);
        assertThat(Matches.without(names, "Alice")).containsExactly("Bob", "Charlie");
        assertThat(Matches.countEqual(names, "Zoe")).isZero();
        assertThat(Matches.without(List.of(1, 2, 1), 1)).containsExactly(2);
    }

    @Test
    void aNullTargetMatchesNulls() {
        List<String> withNulls = Arrays.asList("Alice", null, "Bob", null);

        assertThat(Matches.countEqual(withNulls, null)).isEqualTo(2);
        assertThat(Matches.without(withNulls, null)).containsExactly("Alice", "Bob");
        assertThat(Matches.countEqual(List.of("x"), null)).isZero();
    }

    @Test
    void nullItemsDoNotBreakTheSearch() {
        List<String> withNulls = Arrays.asList(null, "Alice", null, "Bob");

        assertThat(Matches.countEqual(withNulls, new String("Alice"))).isEqualTo(1);
        assertThat(Matches.without(withNulls, new String("Bob"))).containsExactly(null, "Alice", null);
    }

    @Test
    void equalButSeparateObjectsMatch() {
        List<String> names = List.of(new String("Alice"), new String("Bob"), new String("Alice"));
        String target = new StringBuilder("Ali").append("ce").toString();

        assertThat(Matches.countEqual(names, target)).isEqualTo(2);
        assertThat(Matches.without(names, target)).containsExactly("Bob");

        List<Integer> big = List.of(Integer.valueOf(5000), Integer.valueOf(7), Integer.valueOf(5000));
        assertThat(Matches.countEqual(big, Integer.valueOf(5000))).isEqualTo(2);
        assertThat(Matches.without(big, Integer.valueOf(5000))).containsExactly(7);
    }

    @Test
    void withoutAlwaysReturnsANewList() {
        List<String> items = new java.util.ArrayList<>(List.of("Alice", "Bob"));

        List<String> result = Matches.without(items, "Zoe");

        assertThat(result).containsExactly("Alice", "Bob");
        assertThat(result).isNotSameAs(items);
    }
}
