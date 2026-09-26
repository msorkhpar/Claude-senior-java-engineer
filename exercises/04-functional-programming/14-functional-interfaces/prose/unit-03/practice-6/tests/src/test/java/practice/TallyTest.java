package practice;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TallyTest {

    @Test
    void countsEachWord() {
        assertThat(Tally.counts(List.of("java", "python", "java", "java", "python")))
                .isEqualTo(Map.of("java", 3, "python", 2));
        assertThat(Tally.counts(List.of())).isEmpty();
        assertThat(Tally.combine(new HashMap<>(Map.of("a", 1, "b", 2)), new HashMap<>(Map.of("b", 3, "c", 4))))
                .isEqualTo(Map.of("a", 1, "b", 5, "c", 4));
    }

    @Test
    void countingIgnoresCase() {
        assertThat(Tally.counts(List.of("Java", "java", "JAVA", "Go")))
                .isEqualTo(Map.of("java", 3, "go", 1));
    }

    @Test
    void blankWordsAreNotCounted() {
        assertThat(Tally.counts(List.of("", "  ", "kotlin", "\t")))
                .isEqualTo(Map.of("kotlin", 1));
    }

    @Test
    void mergingLeavesBothInputsAlone() {
        Map<String, Integer> a = new HashMap<>(Map.of("a", 1, "b", 2));
        Map<String, Integer> b = new HashMap<>(Map.of("b", 3, "c", 4));

        Tally.combine(a, b);

        assertThat(a).isEqualTo(Map.of("a", 1, "b", 2));
        assertThat(b).isEqualTo(Map.of("b", 3, "c", 4));
    }

    @Test
    void equalWordsBuiltApartAreOneWord() {
        assertThat(Tally.counts(List.of(new String("go"), new String("go"), new String("rust"))))
                .isEqualTo(Map.of("go", 2, "rust", 1));
    }

    @Test
    void combineAlwaysReturnsANewMap() {
        Map<String, Integer> empty = new HashMap<>();
        Map<String, Integer> b = new HashMap<>(Map.of("b", 3));

        Map<String, Integer> result = Tally.combine(empty, b);
        result.put("z", 9);
        Map<String, Integer> other = Tally.combine(b, empty);
        other.put("y", 8);

        assertThat(b).isEqualTo(Map.of("b", 3));
        assertThat(empty).isEmpty();
    }

    @Test
    void unicodeWhitespaceIsBlank() {
        assertThat(Tally.counts(List.of("\u2003", "go"))).isEqualTo(Map.of("go", 1));
    }

    @Test
    void surroundingSpacesArePartOfTheWord() {
        assertThat(Tally.counts(List.of(" go ", "go", "GO"))).isEqualTo(Map.of(" go ", 1, "go", 2));
    }
}
