package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NarrowestTypeTest {

    @Test
    void picksTheNarrowestType() {
        assertThat(NarrowestType.narrowestType(0)).isEqualTo("byte");
        assertThat(NarrowestType.narrowestType(100)).isEqualTo("byte");
        assertThat(NarrowestType.narrowestType(-5)).isEqualTo("byte");
        assertThat(NarrowestType.narrowestType(1000)).isEqualTo("short");
        assertThat(NarrowestType.narrowestType(100_000)).isEqualTo("int");
        assertThat(NarrowestType.narrowestType(10_000_000_000L)).isEqualTo("long");
    }

    @Test
    void upperBoundsBelongToTheType() {
        assertThat(NarrowestType.narrowestType(127)).isEqualTo("byte");
        assertThat(NarrowestType.narrowestType(128)).isEqualTo("short");
        assertThat(NarrowestType.narrowestType(32_767)).isEqualTo("short");
        assertThat(NarrowestType.narrowestType(32_768)).isEqualTo("int");
        assertThat(NarrowestType.narrowestType(Integer.MAX_VALUE)).isEqualTo("int");
    }

    @Test
    void negativeRangesReachOneFurther() {
        assertThat(NarrowestType.narrowestType(-128)).isEqualTo("byte");
        assertThat(NarrowestType.narrowestType(-129)).isEqualTo("short");
        assertThat(NarrowestType.narrowestType(-32_768)).isEqualTo("short");
        assertThat(NarrowestType.narrowestType(Integer.MIN_VALUE)).isEqualTo("int");
    }

    @Test
    void longExtremesAreLong() {
        assertThat(NarrowestType.narrowestType(Long.MAX_VALUE)).isEqualTo("long");
        assertThat(NarrowestType.narrowestType(Long.MIN_VALUE)).isEqualTo("long");
    }
}
