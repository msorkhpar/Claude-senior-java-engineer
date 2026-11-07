package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ArrivalDatesTest {

    @Test
    void readsADateTypedInFull() {
        assertThat(ArrivalDates.arrival("14.11.2031")).contains(LocalDate.of(2031, 11, 14));
        assertThat(ArrivalDates.arrival("03.10.2032")).contains(LocalDate.of(2032, 10, 3));
        assertThat(ArrivalDates.arrival("29.02.2032")).contains(LocalDate.of(2032, 2, 29));
        assertThat(ArrivalDates.bookingRequest("14.11.2031")).isEqualTo("arrival 2031-11-14");
    }

    @Test
    void refusesTextThatIsNotADayMonthYear() {
        assertThat(ArrivalDates.arrival("next friday")).isEmpty();
        assertThat(ArrivalDates.arrival("2031-11-14")).isEmpty();
        assertThat(ArrivalDates.arrival("14.13.2031")).isEmpty();
        assertThat(ArrivalDates.arrival("")).isEmpty();
    }

    @Test
    void refusesADayTheMonthDoesNotHave() {
        assertThat(ArrivalDates.arrival("31.04.2032")).isEmpty();
        assertThat(ArrivalDates.arrival("29.02.2031")).isEmpty();
        assertThat(ArrivalDates.bookingRequest("31.06.2031")).isEqualTo("please check the arrival date");
    }

    @Test
    void readsADayAndMonthTypedWithOneDigit() {
        assertThat(ArrivalDates.arrival("4.7.2031")).contains(LocalDate.of(2031, 7, 4));
        assertThat(ArrivalDates.arrival("9.12.2032")).contains(LocalDate.of(2032, 12, 9));
    }
}
