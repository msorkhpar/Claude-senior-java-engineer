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
        // TODO: A new ticket records when the car came in and covers the free minutes from then.
        throw new IllegalStateException("TODO: enter");
    }

    /** True when the car has stayed past the time its ticket covers. */
    public boolean overstayed(Ticket ticket) {
        // TODO: Has the time the ticket covers run out? The tests run the car park on a clock
        //       of their own and move it forward.
        throw new IllegalStateException("TODO: overstayed");
    }

    /** The ticket after the driver pays for {@code more} time. */
    public Ticket pay(Ticket ticket, Duration more) {
        // TODO: The ticket the driver gets after paying: it covers {@code more} time beyond what
        //       the old one covered.
        throw new IllegalStateException("TODO: pay");
    }
}
