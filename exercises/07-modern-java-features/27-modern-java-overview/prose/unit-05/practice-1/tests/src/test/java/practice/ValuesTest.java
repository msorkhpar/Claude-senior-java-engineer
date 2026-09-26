package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValuesTest {

    @Test
    void formatsByTypeAndGuard() {
        assertThat(Values.format(-5)).isEqualTo("negative integer -5");
        assertThat(Values.format(0)).isEqualTo("non-negative integer 0");
        assertThat(Values.format(1000)).isEqualTo("non-negative integer 1000");
        assertThat(Values.format("")).isEqualTo("blank string");
        assertThat(Values.format("Java 21")).isEqualTo("string Java 21");
        assertThat(Values.format(new ArrayList<String>())).isEqualTo("empty list");
        assertThat(Values.format(List.of("a", "b"))).isEqualTo("list of 2");
        assertThat(Values.format(2.5)).isEqualTo("other: Double");
        assertThat(Values.format(new HashSet<>(List.of(1)))).isEqualTo("other: HashSet");
    }

    @Test
    void nullHasItsOwnCase() {
        assertThat(Values.format(null)).isEqualTo("null");
    }

    @Test
    void whitespaceOnlyStringsAreBlank() {
        assertThat(Values.format("   ")).isEqualTo("blank string");
        assertThat(Values.format("\t\n")).isEqualTo("blank string");
        assertThat(Values.format("\u2003\u2000")).isEqualTo("blank string");
    }
}
