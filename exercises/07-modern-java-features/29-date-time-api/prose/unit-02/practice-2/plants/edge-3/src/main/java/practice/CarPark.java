package practice;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Tickets for a car park: the first 15 minutes are free, and a driver can pay for more time.
 * The barrier controller hands in the clock the car park runs on.
 */
public class CarPark {

    static final Duration FREE = Duration.ofMinutes(15);

    /** One car's ticket. A ticket never changes; paying for more time issues a new one. */
    public record Ticket(String plate, Instant entered, Instant paidUntil) {
        public Ticket {
            Objects.requireNonNull(plate);
            Objects.requireNonNull(entered);
            Objects.requireNonNull(paidUntil);
        }
    }

    private final Clock clock;

    public CarPark(Clock clock) {
        this.clock = clock;
    }

    /** The ticket printed when a car drives in. */
    public Ticket enter(String plate) {
        Instant now = clock.instant();
        return new Ticket(plate, now, now.plus(FREE));
    }

    /** True when the car has stayed past the time its ticket covers. */
    public boolean overstayed(Ticket ticket) {
        return clock.instant().isAfter(ticket.paidUntil());
    }

    /** The ticket after the driver pays for {@code more} time. */
    public Ticket pay(Ticket ticket, Duration more) {
        ticket.paidUntil().plus(more);
        return ticket;
    }
}
