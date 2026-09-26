package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BatchChecksTest {

    @Test
    void checksAFilledBatch() {
        List<String> words = List.of("apple", "banana", "cherry");
        assertThat(BatchChecks.allLongerThan(words, 4)).isTrue();
        assertThat(BatchChecks.allLongerThan(words, 5)).isFalse();
        assertThat(BatchChecks.anyLongerThan(words, 5)).isTrue();
        assertThat(BatchChecks.anyLongerThan(words, 6)).isFalse();
        assertThat(BatchChecks.noneStartsWith(words, "z")).isTrue();
        assertThat(BatchChecks.noneStartsWith(words, "b")).isFalse();
        assertThat(BatchChecks.anyLongerThan(List.of(), 0)).isFalse();
    }

    @Test
    void allOfNothingIsTrue() {
        assertThat(BatchChecks.allLongerThan(List.of(), 100)).isTrue();
    }

    @Test
    void noneOfNothingIsTrue() {
        assertThat(BatchChecks.noneStartsWith(List.of(), "a")).isTrue();
    }

    @Test
    void aPrefixIsMatchedExactlyAtTheStart() {
        assertThat(BatchChecks.noneStartsWith(List.of("cab", "tub"), "b")).isTrue();
        assertThat(BatchChecks.noneStartsWith(List.of("Banana"), "b")).isTrue();
        assertThat(BatchChecks.noneStartsWith(List.of("Banana", "bread"), "b")).isFalse();
    }

    @Test
    void spacesCountTowardsTheLength() {
        assertThat(BatchChecks.allLongerThan(List.of(" ab ", "abcd"), 3)).isTrue();
        assertThat(BatchChecks.anyLongerThan(List.of("  a  "), 4)).isTrue();
    }
}
