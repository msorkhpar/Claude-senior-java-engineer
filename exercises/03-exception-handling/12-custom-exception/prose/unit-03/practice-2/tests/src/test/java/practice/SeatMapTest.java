package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class SeatMapTest {

    @Test
    void reservesFreeSeats() throws Exception {
        SeatMap map = new SeatMap(10);
        map.reserve(1);
        map.reserve(3);
        map.reserve(10);
        assertThat(map.isReserved(1)).isTrue();
        assertThat(map.isReserved(3)).isTrue();
        assertThat(map.isReserved(10)).isTrue();
        assertThat(map.isReserved(2)).isFalse();
    }

    @Test
    void aTakenSeatIsADomainFailure() throws Exception {
        SeatMap map = new SeatMap(10);
        map.reserve(3);
        SeatTakenException e = catchThrowableOfType(() -> map.reserve(3), SeatTakenException.class);
        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("Seat 3 is taken");
        assertThat(e.getSeat()).isEqualTo(3);
    }

    @Test
    void aSeatOutsideTheMapIsABadArgument() {
        SeatMap map = new SeatMap(10);
        assertThatThrownBy(() -> map.reserve(0))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("No such seat: 0");
        assertThatThrownBy(() -> map.reserve(11))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("No such seat: 11");
    }

    @Test
    void aClosedMapIsInTheWrongState() {
        SeatMap map = new SeatMap(10);
        map.close();
        assertThatThrownBy(() -> map.reserve(1))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Booking closed");
    }
}
