package practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class BookingTest {

    private static final long START = Instant.parse("2024-03-15T12:30:00Z").toEpochMilli();

    @Test
    void returnsTheStartItWasGiven() {
        Booking booking = new Booking(new Date(START));
        assertThat(booking.getStart().getTime()).isEqualTo(START);
        assertThat(booking.getStart()).isEqualTo(new Date(START));
        Booking other = new Booking(new Date(START + 3_600_000L));
        assertThat(other.getStart().getTime()).isEqualTo(START + 3_600_000L);
        assertThat(booking.getStart().getTime()).as("each booking keeps its own start").isEqualTo(START);
    }

    @Test
    void theCallersLaterChangeDoesNotReachIt() {
        Date mine = new Date(START);
        Booking booking = new Booking(mine);
        mine.setTime(0);
        assertThat(booking.getStart().getTime()).as("the booking shares the caller's Date").isEqualTo(START);
    }

    @Test
    void aChangeToAReturnedDateDoesNotReachIt() {
        Booking booking = new Booking(new Date(START));
        booking.getStart().setTime(0);
        assertThat(booking.getStart().getTime()).as("getStart hands out the booking's own Date").isEqualTo(START);
    }
}
