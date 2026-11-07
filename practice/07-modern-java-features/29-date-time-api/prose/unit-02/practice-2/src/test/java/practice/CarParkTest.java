package practice;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class CarParkTest {

    /** A clock the test moves by hand. */
    static final class HandClock extends Clock {
        private Instant now;

        HandClock(String start) {
            this.now = Instant.parse(start);
        }

        void advance(Duration by) {
            now = now.plus(by);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException("the car park only reads instants");
        }
    }

    @Test
    void aNewTicketCoversTheFreeMinutesFromEntry() {
        CarPark park = new CarPark(new HandClock("2041-03-09T07:42:10Z"));

        CarPark.Ticket ticket = park.enter("KX-118-B");

        assertThat(ticket.plate()).isEqualTo("KX-118-B");
        assertThat(Duration.between(ticket.entered(), ticket.paidUntil())).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void theTicketShowsTheCarParksTimeOfEntry() {
        CarPark park = new CarPark(new HandClock("2041-03-09T07:42:10Z"));

        CarPark.Ticket ticket = park.enter("PL-4471");

        assertThat(ticket.entered()).isEqualTo(Instant.parse("2041-03-09T07:42:10Z"));
        assertThat(ticket.paidUntil()).isEqualTo(Instant.parse("2041-03-09T07:57:10Z"));
    }

    @Test
    void aCarOverstaysOnceTheClockPassesItsCover() {
        HandClock clock = new HandClock("2041-11-23T18:05:00Z");
        CarPark park = new CarPark(clock);
        CarPark.Ticket ticket = new CarPark.Ticket("DE-77-QR",
                Instant.parse("2041-11-23T18:05:00Z"), Instant.parse("2041-11-23T18:20:00Z"));

        clock.advance(Duration.ofMinutes(14));
        assertThat(park.overstayed(ticket)).isFalse();

        clock.advance(Duration.ofMinutes(2));
        assertThat(park.overstayed(ticket)).isTrue();
    }

    @Test
    void payingExtendsTheCover() {
        CarPark park = new CarPark(new HandClock("2041-03-09T07:42:10Z"));
        CarPark.Ticket ticket = new CarPark.Ticket("TR-0193",
                Instant.parse("2041-03-09T06:00:00Z"), Instant.parse("2041-03-09T07:30:00Z"));

        CarPark.Ticket paid = park.pay(ticket, Duration.ofMinutes(45));

        assertThat(paid.paidUntil()).isEqualTo(Instant.parse("2041-03-09T08:15:00Z"));
        assertThat(paid.entered()).isEqualTo(Instant.parse("2041-03-09T06:00:00Z"));
        assertThat(paid.plate()).isEqualTo("TR-0193");
    }
}
