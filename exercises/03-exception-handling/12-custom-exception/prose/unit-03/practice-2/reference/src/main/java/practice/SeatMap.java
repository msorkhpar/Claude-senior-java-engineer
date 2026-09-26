package practice;

/** Checked: the seat is already reserved. Carries the seat number. */
class SeatTakenException extends Exception {

    private final int seat;

    SeatTakenException(String message, int seat) {
        super(message);
        this.seat = seat;
    }

    int getSeat() {
        return seat;
    }
}

public class SeatMap {

    private final boolean[] reserved;
    private boolean closed;

    public SeatMap(int capacity) {
        this.reserved = new boolean[capacity];
    }

    /** Reserves a seat in 1..capacity; see the statement for each failure. */
    public void reserve(int seat) throws SeatTakenException {
        if (closed) {
            throw new IllegalStateException("Booking closed");
        }
        if (seat < 1 || seat > reserved.length) {
            throw new IllegalArgumentException("No such seat: " + seat);
        }
        if (reserved[seat - 1]) {
            throw new SeatTakenException("Seat " + seat + " is taken", seat);
        }
        reserved[seat - 1] = true;
    }

    public boolean isReserved(int seat) {
        return reserved[seat - 1];
    }

    public void close() {
        closed = true;
    }
}
