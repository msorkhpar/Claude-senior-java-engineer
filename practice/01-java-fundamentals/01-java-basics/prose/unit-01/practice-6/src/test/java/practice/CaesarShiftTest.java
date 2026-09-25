package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaesarShiftTest {

    @Test
    void shiftsLowercaseLetters() {
        assertThat(CaesarShift.shift("abc", 1)).isEqualTo("bcd");
        assertThat(CaesarShift.shift("hello", 3)).isEqualTo("khoor");
        assertThat(CaesarShift.shift("java", 0)).isEqualTo("java");
    }

    @Test
    void wrapsPastZ() {
        assertThat(CaesarShift.shift("xyz", 3)).isEqualTo("abc");
        assertThat(CaesarShift.shift("z", 25)).isEqualTo("y");
    }

    @Test
    void keepsTheCase() {
        assertThat(CaesarShift.shift("Hello", 3)).isEqualTo("Khoor");
        assertThat(CaesarShift.shift("XYZ", 3)).isEqualTo("ABC");
    }

    @Test
    void leavesOtherCharactersAlone() {
        assertThat(CaesarShift.shift("a-b c!", 1)).isEqualTo("b-c d!");
        assertThat(CaesarShift.shift("2024", 5)).isEqualTo("2024");
    }
}
