package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SaturatingBytesTest {

    private static byte add(int a, int b) {
        return SaturatingBytes.addSaturating((byte) a, (byte) b);
    }

    @Test
    void addsWithinTheRange() {
        assertThat(add(10, 20)).isEqualTo((byte) 30);
        assertThat(add(-5, 3)).isEqualTo((byte) -2);
        assertThat(add(100, 27)).isEqualTo((byte) 127);
        assertThat(add(-100, -28)).isEqualTo((byte) -128);
    }

    @Test
    void holdsAtTheTop() {
        assertThat(add(100, 100)).isEqualTo((byte) 127);
        assertThat(add(127, 1)).isEqualTo((byte) 127);
    }

    @Test
    void holdsAtTheBottom() {
        assertThat(add(-100, -100)).isEqualTo((byte) -128);
        assertThat(add(-128, -1)).isEqualTo((byte) -128);
    }
}
