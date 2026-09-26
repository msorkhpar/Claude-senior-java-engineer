package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ReductionsTest {

    @Test
    void multipliesAndFindsTheLargest() {
        assertThat(Reductions.product(List.of(1, 2, 3, 4))).isEqualTo(24);
        assertThat(Reductions.product(List.of(7))).isEqualTo(7);
        assertThat(Reductions.product(List.of(2, 0, 5))).isEqualTo(0);
        assertThat(Reductions.largest(List.of(5, 3, 8, 1))).isEqualTo(Optional.of(8));
        assertThat(Reductions.largest(List.of(4))).isEqualTo(Optional.of(4));
    }

    @Test
    void findsTheFirstMatch() {
        List<String> words = List.of("cherry", "banana", "blueberry");
        assertThat(Reductions.firstStartingWith(words, "b", "none")).isEqualTo("banana");
        assertThat(Reductions.firstStartingWith(words, "blue", "none")).isEqualTo("blueberry");
        assertThat(Reductions.firstStartingWith(words, "", "none")).isEqualTo("cherry");
    }

    @Test
    void productOfNothingIsOne() {
        assertThat(Reductions.product(List.of())).isEqualTo(1);
    }

    @Test
    void largestOfNegativesIsNegative() {
        assertThat(Reductions.largest(List.of(-7, -3, -9))).isEqualTo(Optional.of(-3));
    }

    @Test
    void largestOfNothingIsEmpty() {
        assertThat(Reductions.largest(List.of())).isEmpty();
    }

    @Test
    void noMatchGivesTheFallback() {
        assertThat(Reductions.firstStartingWith(List.of("cherry", "banana"), "z", "none")).isEqualTo("none");
        assertThat(Reductions.firstStartingWith(List.of(), "a", "nothing")).isEqualTo("nothing");
    }

    @Test
    void thePrefixIsPlainTextAtTheStart() {
        assertThat(Reductions.firstStartingWith(List.of("crab", "Bay", "bat"), "b", "none")).isEqualTo("bat");
        assertThat(Reductions.firstStartingWith(List.of("bax", "b.y"), "b.", "none")).isEqualTo("b.y");
    }
}
