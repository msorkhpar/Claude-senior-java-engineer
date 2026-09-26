package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class QuartersTest {

    @Test
    void findsTheQuarterAndItsEnd() {
        assertThat(Quarters.quarterOf(LocalDate.of(2024, 8, 15))).isEqualTo(3);
        assertThat(Quarters.quarterOf(LocalDate.of(2024, 5, 1))).isEqualTo(2);
        assertThat(Quarters.quarterOf(LocalDate.of(2024, 1, 10))).isEqualTo(1);
        assertThat(LocalDate.of(2024, 8, 15).with(Quarters.endOfQuarter())).isEqualTo(LocalDate.of(2024, 9, 30));
        assertThat(LocalDate.of(2024, 5, 1).with(Quarters.endOfQuarter())).isEqualTo(LocalDate.of(2024, 6, 30));
    }

    @Test
    void aQuarterEndsWithItsLastMonth() {
        assertThat(Quarters.quarterOf(LocalDate.of(2024, 3, 31))).isEqualTo(1);
        assertThat(Quarters.quarterOf(LocalDate.of(2024, 9, 30))).isEqualTo(3);
        assertThat(Quarters.quarterOf(LocalDate.of(2024, 12, 1))).isEqualTo(4);
    }

    @Test
    void aQuarterCanEndOnThe31st() {
        assertThat(LocalDate.of(2024, 2, 10).with(Quarters.endOfQuarter())).isEqualTo(LocalDate.of(2024, 3, 31));
        assertThat(LocalDate.of(2024, 11, 5).with(Quarters.endOfQuarter())).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    void keepsTheTimeOfADateTime() {
        assertThat(LocalDateTime.of(2024, 8, 15, 10, 15).with(Quarters.endOfQuarter()))
                .isEqualTo(LocalDateTime.of(2024, 9, 30, 10, 15));
    }
}
