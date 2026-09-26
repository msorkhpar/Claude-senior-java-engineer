package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FirstMatchTest {

    private static final List<String> NAMES = List.of("Alice", "Bob", "Charlie", "David", "Eve");

    @Test
    void findsTheFirstMatch() {
        assertThat(FirstMatch.firstMatch(NAMES, s -> s.startsWith("C"), new ArrayList<>()))
                .contains("Charlie");
        assertThat(FirstMatch.firstMatch(NAMES, s -> s.length() == 3, new ArrayList<>()))
                .contains("Bob");
    }

    @Test
    void stopsExaminingAtTheFirstMatch() {
        List<String> examined = new ArrayList<>(List.of("earlier"));

        Optional<String> found = FirstMatch.firstMatch(NAMES, s -> s.startsWith("C"), examined);

        assertThat(found).contains("Charlie");
        assertThat(examined).containsExactly("earlier", "Alice", "Bob", "Charlie");
    }

    @Test
    void noMatchIsAnEmptyOptional() {
        List<String> examined = new ArrayList<>();

        Optional<String> found = FirstMatch.firstMatch(NAMES, s -> s.startsWith("Z"), examined);

        assertThat(found).isEmpty();
        assertThat(examined).containsExactlyElementsOf(NAMES);
        assertThat(FirstMatch.firstMatch(List.of(), s -> true, new ArrayList<>())).isEmpty();
    }
}
