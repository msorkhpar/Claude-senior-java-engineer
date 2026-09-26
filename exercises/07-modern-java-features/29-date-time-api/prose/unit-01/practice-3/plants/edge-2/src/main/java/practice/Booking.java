package practice;

import java.util.Date;

public final class Booking {

    private final Date start;

    public Booking(Date start) {
        this.start = new Date(start.getTime());
    }

    public Date getStart() {
        return start; // hands out its own Date
    }
}
