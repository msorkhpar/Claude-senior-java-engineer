package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NarrowingTest {

    @Test
    void keepsValuesThatFit() {
        assertThat(Narrowing.clampToInt(0L)).isEqualTo(0);
        assertThat(Narrowing.clampToInt(42L)).isEqualTo(42);
        assertThat(Narrowing.clampToInt(-42L)).isEqualTo(-42);
        assertThat(Narrowing.clampToInt(1_234_567_890L)).isEqualTo(1_234_567_890);
        assertThat(Narrowing.clampToInt(Integer.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
        assertThat(Narrowing.clampToInt(Integer.MIN_VALUE)).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void clampsAboveTheRange() {
        assertThat(Narrowing.clampToInt(2_147_483_648L)).isEqualTo(Integer.MAX_VALUE);
        assertThat(Narrowing.clampToInt(Long.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
        assertThat(Narrowing.clampToInt(4_294_967_296L)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void clampsBelowTheRange() {
        assertThat(Narrowing.clampToInt(-2_147_483_649L)).isEqualTo(Integer.MIN_VALUE);
        assertThat(Narrowing.clampToInt(Long.MIN_VALUE)).isEqualTo(Integer.MIN_VALUE);
    }
}
