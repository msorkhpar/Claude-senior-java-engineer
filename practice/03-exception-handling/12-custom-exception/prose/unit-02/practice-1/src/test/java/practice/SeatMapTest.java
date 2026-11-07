package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SeatMapTest {

    private static SeatMap flight() {
        SeatMap flight = new SeatMap("3A", "3B", "3C", "17D", "17E", "29F");
        flight.assign("M. Kowalski", "3A");
        flight.assign("T. Nakamura", "17E");
        return flight;
    }

    @Test
    void movesThePassengerToTheFreeSeat() {
        SeatMap flight = flight();
        flight.rebook("M. Kowalski", "3A", "29F");
        assertThat(flight.holder("29F")).isEqualTo("M. Kowalski");
        assertThat(flight.holder("3A")).isNull();
        assertThat(flight.holder("17E")).isEqualTo("T. Nakamura");
    }

    @Test
    void aPassengerCanMoveAgainAndBack() {
        SeatMap flight = flight();
        flight.rebook("T. Nakamura", "17E", "3B");
        flight.rebook("T. Nakamura", "3B", "17D");
        flight.rebook("T. Nakamura", "17D", "17E");
        assertThat(flight.holder("17E")).isEqualTo("T. Nakamura");
        assertThat(flight.holder("3B")).isNull();
        assertThat(flight.holder("17D")).isNull();
    }

    @Test
    void aTargetSeatNotOnTheFlightIsRefusedAndThePassengerKeepsTheirSeat() {
        SeatMap flight = flight();
        Map<String, String> before = flight.snapshot();
        assertThatThrownBy(() -> flight.rebook("M. Kowalski", "3A", "41K"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("41K");
        assertThat(flight.snapshot()).isEqualTo(before);
    }

    @Test
    void aTakenTargetSeatIsRefusedAndBothPassengersKeepTheirSeats() {
        SeatMap flight = flight();
        Map<String, String> before = flight.snapshot();
        assertThatThrownBy(() -> flight.rebook("M. Kowalski", "3A", "17E"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("17E");
        assertThat(flight.snapshot()).isEqualTo(before);
    }

    @Test
    void aSeatHeldBySomeoneElseCannotBeGivenUp() {
        SeatMap flight = flight();
        Map<String, String> before = flight.snapshot();
        assertThatThrownBy(() -> flight.rebook("M. Kowalski", "17E", "3C"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("17E");
        assertThat(flight.snapshot()).isEqualTo(before);
    }
}
