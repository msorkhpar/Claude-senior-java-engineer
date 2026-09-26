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
}
