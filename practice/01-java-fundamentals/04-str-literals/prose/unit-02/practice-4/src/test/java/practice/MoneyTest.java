package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void formatsAnAmount() {
        assertThat(Money.format("Ada", 1250)).isEqualTo("Ada: $12.50");
        assertThat(Money.format("Eve", 9999)).isEqualTo("Eve: $99.99");
    }

    @Test
    void fewCentsArePadded() {
        assertThat(Money.format("Ada", 1205)).isEqualTo("Ada: $12.05");
        assertThat(Money.format("Bob", 7)).isEqualTo("Bob: $0.07");
        assertThat(Money.format("Cy", 300)).isEqualTo("Cy: $3.00");
    }
}
