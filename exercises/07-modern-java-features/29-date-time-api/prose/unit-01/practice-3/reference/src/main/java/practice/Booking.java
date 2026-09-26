package practice;

import java.util.Date;

public final class Booking {

    private final Date start;

    public Booking(Date start) {
        // copy in: the caller keeps its own Date and may change it later
        this.start = new Date(start.getTime());
    }

    /** Returns the start of the booking. */
    public Date getStart() {
        // copy out: the caller may change the Date it gets back
        return new Date(start.getTime());
    }
}
