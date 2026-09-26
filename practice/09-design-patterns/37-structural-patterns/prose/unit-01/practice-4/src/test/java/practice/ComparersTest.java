package practice;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComparersTest {

    private static final Comparers.LegacyStringComparer BY_LENGTH = (a, b) -> a.length() < b.length();

    @Test
    void ordersByTheLegacyRule() {
        Comparator<String> cmp = Comparers.asComparator(BY_LENGTH);

        assertThat(cmp.compare("a", "bb")).isNegative();
        assertThat(Collections.min(List.of("ccc", "a", "bb"), cmp)).isEqualTo("a");
    }

    @Test
    void neitherLessMeansEqual() {
        Comparator<String> cmp = Comparers.asComparator(BY_LENGTH);

        assertThat(cmp.compare("abc", "xyz")).isZero();
        assertThat(cmp.compare("q", "q")).isZero();
    }

    @Test
    void greaterIsPositive() {
        Comparator<String> cmp = Comparers.asComparator(BY_LENGTH);

        assertThat(cmp.compare("abcd", "ab")).isPositive();
    }
}
