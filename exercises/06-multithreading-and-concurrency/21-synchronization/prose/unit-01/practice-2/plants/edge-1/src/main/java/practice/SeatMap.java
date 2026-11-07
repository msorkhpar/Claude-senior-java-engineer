package practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The seats of one performance. Many request threads book seats at once; a booking charges the
 * buyer's card through the payment gateway, which takes a second or two.
 */
public class SeatMap {

    public enum State { FREE, HELD, SOLD }

    /** Charges a card. True when the charge went through, false when the card was declined. */
    public interface Gateway {
        boolean charge(String card, long cents);
    }

    private final Object lock = new Object();
    private final Map<String, State> seats = new HashMap<>();
    private final long priceCents;
    private final Gateway gateway;

    public SeatMap(List<String> seatIds, long priceCents, Gateway gateway) {
        for (String id : seatIds) seats.put(id, State.FREE);
        this.priceCents = priceCents;
        this.gateway = gateway;
    }

    public State state(String seat) {
        synchronized (lock) {
            return seats.get(seat);
        }
    }

    /**
     * Sells a seat to the holder of the card. True when the seat is now theirs; false when it
     * wasn't free or the card was declined. An exception from the gateway reaches the caller.
     */
    public boolean book(String seat, String card) {
        synchronized (lock) {
            if (seats.get(seat) != State.FREE) return false;
        }
        boolean paid = gateway.charge(card, priceCents);
        synchronized (lock) {
            if (paid) seats.put(seat, State.SOLD);
        }
        return paid;
    }
}
