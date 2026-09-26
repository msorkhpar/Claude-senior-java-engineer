package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DescribeTest {

    @Test
    void describesEachKindOfValue() {
        assertThat(Describe.describe("hi")).isEqualTo("string of length 2");
        assertThat(Describe.describe("")).isEqualTo("string of length 0");
        assertThat(Describe.describe(0)).isEqualTo("integer 0");
        assertThat(Describe.describe(-500)).isEqualTo("integer -500");
        assertThat(Describe.describe(List.of(1, 2, 3))).isEqualTo("list of 3");
        assertThat(Describe.describe(new ArrayList<String>())).isEqualTo("list of 0");
        assertThat(Describe.describe(2.5)).isEqualTo("other: Double");
        assertThat(Describe.describe(new StringBuilder("sb"))).isEqualTo("other: StringBuilder");
        assertThat(Describe.describe(new HashSet<>(List.of(1)))).isEqualTo("other: HashSet");
        assertThat(Describe.describe(5L)).isEqualTo("other: Long");
    }

    @Test
    void nullIsDescribedSafely() {
        assertThat(Describe.describe(null)).isEqualTo("null");
    }

    @Test
    void guardedCasesComeFirst() {
        assertThat(Describe.describe("hello, world")).isEqualTo("long string: hello, world");
        assertThat(Describe.describe(1000)).isEqualTo("positive integer 1000");
        assertThat(Describe.describe("exactly 10")).isEqualTo("string of length 10");
    }
}
