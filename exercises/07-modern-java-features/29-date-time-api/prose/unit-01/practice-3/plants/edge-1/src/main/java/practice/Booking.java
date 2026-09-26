package practice;

import java.util.Date;

public final class Booking {

    private final Date start;

    public Booking(Date start) {
        this.start = start; // keeps the caller's own Date
    }

    public Date getStart() {
        return new Date(start.getTime());
    }
}
