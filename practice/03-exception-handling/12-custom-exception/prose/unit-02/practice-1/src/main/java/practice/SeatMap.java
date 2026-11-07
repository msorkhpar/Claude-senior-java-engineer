package practice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** The seats of one flight and who holds each of them. */
public class SeatMap {

    private final Map<String, String> holders = new LinkedHashMap<>();   // seat -> passenger, null when free

    /** A flight with these seats, all free. */
    public SeatMap(String... seats) {
        for (String seat : seats) {
            holders.put(seat, null);
        }
    }

    /** Check-in: gives a free seat to a passenger. */
    public void assign(String passenger, String seat) {
        Objects.requireNonNull(passenger, "passenger");
        if (!holders.containsKey(seat)) throw new IllegalArgumentException("no seat " + seat + " on this flight");
        if (holders.get(seat) != null) throw new IllegalArgumentException("seat " + seat + " is taken");
        holders.put(seat, passenger);
    }

    /** Who holds the seat, or null when it is free. */
    public String holder(String seat) {
        return holders.get(seat);
    }

    /** A copy of the whole map, seat to passenger. */
    public Map<String, String> snapshot() {
        return new LinkedHashMap<>(holders);
    }

    /**
     * Moves a passenger from the seat they hold to a free seat of this flight.
     * A request that can't be carried out throws IllegalArgumentException naming the seat at fault,
     * and leaves every seat as it was.
     */
    public void rebook(String passenger, String fromSeat, String toSeat) {
        // TODO: move the passenger, or refuse the request without changing any seat.
        throw new IllegalStateException("TODO: rebook");
    }
}
