package practice;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class LongestTest {

    @Test
    void findsTheLongest() {
        assertThat(Longest.longest(List.of("a", "abc", "ab"))).contains("abc");
        assertThat(Longest.longest(List.of("xy"))).contains("xy");
    }

    @Test
    void theFirstOfEqualLengthsWins() {
        assertThat(Longest.longest(List.of("one", "two", "six"))).contains("one");
        assertThat(Longest.longest(List.of("a", "bb", "cc"))).contains("bb");
    }

    @Test
    void nullElementsAreIgnored() {
        assertThat(Longest.longest(Arrays.asList(null, "ab", null))).contains("ab");
        assertThat(Longest.longest(Arrays.asList((String) null))).isEmpty();
    }

    @Test
    void noStringsGiveNothing() {
        assertThat(Longest.longest(List.of())).isEmpty();
        assertThat(Longest.longest(null)).isEmpty();
    }
}
