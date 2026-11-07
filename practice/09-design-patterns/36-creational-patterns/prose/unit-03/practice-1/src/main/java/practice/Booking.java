package practice;

import java.time.LocalDateTime;

/**
 * A meeting-room booking. The booking form creates it through its Builder, calling a setter each
 * time the user fills in or changes a field.
 */
public final class Booking {

    /** Refuses a booking; the form shows the message next to the fields. */
    public static final class InvalidBooking extends RuntimeException {
        public InvalidBooking(String message) {
            super(message);
        }
    }

    private final String room;
    private final String organiser;
    private final LocalDateTime start;
    private final LocalDateTime end;

    private Booking(Builder b) {
        this.room = b.room;
        this.organiser = b.organiser;
        this.start = b.start;
        this.end = b.end;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String room() { return room; }
    public String organiser() { return organiser; }
    public LocalDateTime start() { return start; }
    public LocalDateTime end() { return end; }

    public static final class Builder {
        private String room;
        private String organiser;
        private LocalDateTime start;
        private LocalDateTime end;

        private Builder() {}

        public Builder room(String room) { this.room = room; return this; }
        public Builder organiser(String organiser) { this.organiser = organiser; return this; }
        public Builder start(LocalDateTime start) { this.start = start; return this; }
        public Builder end(LocalDateTime end) { this.end = end; return this; }

        /** Creates the booking the form describes, or refuses it with InvalidBooking. */
        public Booking build() {
            // TODO: The calendar shows bookings that end before they start, and bookings with no
            //       organiser. Refuse every booking that can't take place with InvalidBooking, and
            //       return the booking otherwise (new Booking(this)).
            throw new IllegalStateException("TODO: build");
        }
    }
}
