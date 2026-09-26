package practice;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DescribeTest {

    @Test
    void describesByType() {
        assertThat(Describe.describe("hello")).isEqualTo("String of length 5");
        assertThat(Describe.describe(42)).isEqualTo("Positive integer");
        assertThat(Describe.describe(-7)).isEqualTo("Non-positive integer");
        assertThat(Describe.describe(3.5)).isEqualTo("Something else");
    }

    @Test
    void zeroIsNotPositive() {
        assertThat(Describe.describe(0)).isEqualTo("Non-positive integer");
        assertThat(Describe.describe(1)).isEqualTo("Positive integer");
    }

    @Test
    void listsAreCounted() {
        assertThat(Describe.describe(List.of(1, 2))).isEqualTo("List with 2 elements");
        assertThat(Describe.describe(List.of())).isEqualTo("List with 0 elements");
    }

    @Test
    void nullIsNullInput() {
        assertThat(Describe.describe(null)).isEqualTo("Null input");
    }
}
