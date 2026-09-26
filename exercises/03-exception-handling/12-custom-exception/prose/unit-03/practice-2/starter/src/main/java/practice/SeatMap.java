package practice;

/** Checked: the seat is already reserved. Carries the seat number. */
class SeatTakenException extends Exception {

    SeatTakenException(String message, int seat) {
        throw new UnsupportedOperationException("write SeatTakenException(String, int)");
    }

    int getSeat() {
        throw new UnsupportedOperationException("write getSeat");
    }
}

public class SeatMap {

    public SeatMap(int capacity) {
    }

    /** Reserves a seat in 1..capacity; see the statement for each failure. */
    public void reserve(int seat) throws SeatTakenException {
        throw new UnsupportedOperationException("write reserve");
    }

    public boolean isReserved(int seat) {
        throw new UnsupportedOperationException("write isReserved");
    }

    public void close() {
        throw new UnsupportedOperationException("write close");
    }
}
