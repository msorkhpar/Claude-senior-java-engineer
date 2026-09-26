package practice;

import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegacyFieldsTest {

    @Test
    void convertsBothWays() {
        assertThat(LegacyFields.fromLegacy(124, 2, 15)).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(LegacyFields.fromLegacy(124, 0, 1)).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(LegacyFields.fromLegacy(99, 11, 31)).isEqualTo(LocalDate.of(1999, 12, 31));
        assertThat(LegacyFields.fromLegacy(124, 1, 29)).isEqualTo(LocalDate.of(2024, 2, 29));
        assertThat(LegacyFields.toLegacy(LocalDate.of(2024, 3, 15))).containsExactly(124, 2, 15);
        assertThat(LegacyFields.toLegacy(LocalDate.of(1999, 12, 31))).containsExactly(99, 11, 31);
    }

    @Test
    void monthIndexTwelveIsRefused() {
        assertThatThrownBy(() -> LegacyFields.fromLegacy(124, 12, 1))
                .as("month index 12 is not January of the next year")
                .isInstanceOf(DateTimeException.class);
    }

    @Test
    void februaryThirtiethIsRefused() {
        assertThatThrownBy(() -> LegacyFields.fromLegacy(124, 1, 30))
                .as("February 30 is not early March")
                .isInstanceOf(DateTimeException.class);
        assertThatThrownBy(() -> LegacyFields.fromLegacy(0, 1, 29))
                .as("1900 was not a leap year")
                .isInstanceOf(DateTimeException.class);
    }
}
