package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TriangleTest {

    @Test
    void addsOneToN() {
        assertThat(Triangle.sum(5)).isEqualTo(15L);
        assertThat(Triangle.sum(1)).isEqualTo(1L);
        assertThat(Triangle.sum(10)).isEqualTo(55L);
    }

    @Test
    void noPassRunsBelowOne() {
        assertThat(Triangle.sum(0)).isZero();
        assertThat(Triangle.sum(-3)).isZero();
    }

    @Test
    void aLargeNDoesNotOverflow() {
        assertThat(Triangle.sum(100000)).isEqualTo(5000050000L);
        assertThat(Triangle.sum(65536)).isEqualTo(2147516416L);
    }
}
