package practice;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * Seat holds for one flight. The map (seat to customer) belongs to the flight and is shared: every
 * booking request builds its own SeatHolds over it, so requests for one seat can run on several
 * threads at once.
 */
public class SeatHolds {

    private final ConcurrentMap<String, String> holders;

    public SeatHolds(ConcurrentMap<String, String> holders) {
        this.holders = holders;
    }

    /** Who holds the seat now, if anyone. */
    public Optional<String> holderOf(String seat) {
        return Optional.ofNullable(holders.get(seat));
    }

    /** Puts the seat on hold for the customer. True when this customer now holds it. */
    public boolean reserve(String seat, String customer) {
        // TODO: Two requests for the same free seat may arrive at the same moment. Exactly one of
        //       them may get it, and the map must name the customer who was told yes.
        throw new IllegalStateException("TODO: reserve");
    }

    /** Gives up the customer's hold on the seat. True when a hold of this customer was removed. */
    public boolean release(String seat, String customer) {
        // TODO: A customer's release may arrive late, for example a second click, after the seat
        //       has already gone to someone else. It must never free another customer's hold.
        throw new IllegalStateException("TODO: release");
    }
}
