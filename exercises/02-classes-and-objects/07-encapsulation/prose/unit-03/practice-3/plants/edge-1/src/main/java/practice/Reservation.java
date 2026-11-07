package practice;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;

/**
 * A hotel reservation. The web tier keeps reservations in the user's session, and the session
 * store writes sessions out as serialized objects and reads them back on another node.
 */
public final class Reservation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String room;
    private final int guests;
    private final int nights;

    public Reservation(String room, int guests, int nights) {
        check(room, guests, nights);
        this.room = room;
        this.guests = guests;
        this.nights = nights;
    }

    /** Refuses a missing or blank room, 1 to 4 guests only, 1 to 28 nights only. */
    static void check(String room, int guests, int nights) {
        if (room == null || room.isBlank()) throw new IllegalArgumentException("room");
        if (guests < 1 || guests > 4) throw new IllegalArgumentException("guests " + guests);
        if (nights < 1 || nights > 28) throw new IllegalArgumentException("nights " + nights);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public String room() { return room; }

    public int guests() { return guests; }

    public int nights() { return nights; }

    public long priceCents(long nightlyCents) {
        return nightlyCents * nights + 1_500L * (guests - 1) * nights;
    }
}
