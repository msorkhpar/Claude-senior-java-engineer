package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HundredDividerTest {

    @Test
    void dividesAHundred() {
        assertThat(HundredDivider.describe("4")).isEqualTo("Result: 25");
        assertThat(HundredDivider.describe("3")).isEqualTo("Result: 33");
        assertThat(HundredDivider.describe("100")).isEqualTo("Result: 1");
        assertThat(HundredDivider.describe("101")).isEqualTo("Result: 0");
    }

    @Test
    void rejectsANegativeValue() {
        assertThat(HundredDivider.describe("-5")).isEqualTo("Invalid input: Value must be non-negative");
        assertThat(HundredDivider.describe("-101")).isEqualTo("Invalid input: Value must be non-negative");
    }

    @Test
    void textIsNotANumber() {
        assertThat(HundredDivider.describe("abc")).isEqualTo("Not a number: For input string: \"abc\"");
        assertThat(HundredDivider.describe(new String(""))).isEqualTo("Not a number: For input string: \"\"");
        assertThat(HundredDivider.describe("-abc")).isEqualTo("Not a number: For input string: \"-abc\"");
        assertThat(HundredDivider.describe(" 4")).isEqualTo("Not a number: For input string: \" 4\"");
    }

    @Test
    void zeroIsInvalidBecauseIntegerDivisionThrows() {
        assertThat(HundredDivider.describe("0")).isEqualTo("Invalid input: / by zero");
        assertThat(HundredDivider.describe("-0")).isEqualTo("Invalid input: / by zero");
    }
}
