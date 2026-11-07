package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DeadlinesTest {

    /** Monday 29 March 2027 is a public holiday in these tests. */
    private static final LocalDate HOLIDAY = LocalDate.of(2027, 3, 29);

    private final Deadlines deadlines = new Deadlines(Set.of(HOLIDAY));

    private static LocalDate march(int day) {
        return LocalDate.of(2027, 3, day);
    }

    @Test
    void anOrderOnTuesdayShipsByThursday() {
        assertThat(deadlines.shipBy(march(9))).isEqualTo(march(11));
    }

    @Test
    void aTicketOpenedOnTuesdayIsDueOnWednesday() {
        assertThat(deadlines.replyBy(march(9))).isEqualTo(march(10));
    }

    @Test
    void anOrderOnTheFridayBeforeAHolidayShipsOnTheHoliday() {
        assertThat(deadlines.shipBy(march(26))).isEqualTo(HOLIDAY);
    }

    @Test
    void anOrderOnThursdayShipsBySaturday() {
        assertThat(deadlines.shipBy(march(11))).isEqualTo(march(13));
    }

    @Test
    void anOrderPlacedOnSundayShipsByTuesday() {
        assertThat(deadlines.shipBy(march(14))).isEqualTo(march(16));
    }

    @Test
    void aTicketOpenedOnSaturdayIsDueOnTuesday() {
        assertThat(deadlines.replyBy(march(13))).isEqualTo(march(16));
    }

    @Test
    void aTicketOpenedOnTheFridayBeforeAHolidayIsDueAfterTheHoliday() {
        assertThat(deadlines.replyBy(march(26))).isEqualTo(march(30));
    }

    @Test
    void aTicketOpenedOnFridayIsDueOnMonday() {
        assertThat(deadlines.replyBy(march(12))).isEqualTo(march(15));
    }
}
