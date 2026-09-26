package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReductionsTest {

    @Test
    void findsTheLongestWord() {
        assertThat(Reductions.longest(List.of("hi", "hello", "hey"))).contains("hello");
        assertThat(Reductions.longest(List.of("a"))).contains("a");
    }

    @Test
    void multipliesTheValues() {
        assertThat(Reductions.product(new int[] {1, 2, 3, 4, 5})).isEqualTo(120);
        assertThat(Reductions.product(new int[] {7})).isEqualTo(7);
        assertThat(Reductions.product(new int[] {-2, 3})).isEqualTo(-6);
    }

    @Test
    void noWordsMeansNoLongest() {
        assertThat(Reductions.longest(List.of())).isEmpty();
    }

    @Test
    void aTieKeepsTheFirstWord() {
        assertThat(Reductions.longest(List.of("hey", "abc", "hi"))).contains("hey");
        assertThat(Reductions.longest(List.of("to", "hello", "world"))).contains("hello");
    }

    @Test
    void anEmptyProductIsOne() {
        assertThat(Reductions.product(new int[] {})).isEqualTo(1);
    }
}
