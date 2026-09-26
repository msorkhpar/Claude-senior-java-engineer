package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class CompositionsTest {

    @Test
    void buildsPipelinesAndFilters() {
        Function<Integer, Integer> addOne = x -> x + 1;
        assertThat(Compositions.inOrder(List.of(addOne)).apply(1)).isEqualTo(2);

        assertThat(Compositions.lengthLabel().apply("  HELLO  ")).isEqualTo("Length: 5");
        assertThat(Compositions.lengthLabel().apply("abc")).isEqualTo("Length: 3");

        String empty = new StringBuilder("x").deleteCharAt(0).toString();
        List<String> names = List.of("Alice", "Bob", "Alexander", empty, "Amy", "Barbara");
        assertThat(names.stream().filter(Compositions.acceptedName()).toList())
                .containsExactly("Alice", "Alexander", "Amy", "Barbara");
    }

    @Test
    void stepsRunInListOrder() {
        Function<Integer, Integer> addOne = x -> x + 1;
        Function<Integer, Integer> twice = x -> x * 2;

        assertThat(Compositions.inOrder(List.of(addOne, twice)).apply(3)).isEqualTo(8);
        assertThat(Compositions.inOrder(List.of(twice, addOne)).apply(3)).isEqualTo(7);
    }

    @Test
    void noStepsIsIdentity() {
        List<Function<String, String>> none = List.of();

        assertThat(Compositions.inOrder(none).apply("same")).isEqualTo("same");
    }

    @Test
    void nullNameIsRejectedWithoutThrowing() {
        assertThat(Compositions.acceptedName().test(null)).isFalse();
    }
}
