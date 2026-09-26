package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class VarargsTest {

    @Test
    void addsAndComparesTheArguments() {
        assertThat(Varargs.sum(1, 2, 3)).isEqualTo(6);
        assertThat(Varargs.sum(10, 20)).isEqualTo(30);
        assertThat(Varargs.max(3, 9, 4)).isEqualTo(9);
        assertThat(Varargs.max(1, 2, 8)).isEqualTo(8);
    }

    @Test
    void noNumbersSumToZero() {
        assertThat(Varargs.sum()).isZero();
        assertThat(Varargs.sum((int[]) null)).isZero();
    }

    @Test
    void theFirstArgumentCounts() {
        assertThat(Varargs.max(5)).isEqualTo(5);
        assertThat(Varargs.max(9, 3, 4)).isEqualTo(9);
        assertThat(Varargs.max(-2, -7)).isEqualTo(-2);
    }
}
