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
        return holders.putIfAbsent(seat, customer) == null;
    }

    /** Gives up the customer's hold on the seat. True when a hold of this customer was removed. */
    public boolean release(String seat, String customer) {
        if (!customer.equals(holders.get(seat))) return false;
        holders.remove(seat);
        return true;
    }
}
