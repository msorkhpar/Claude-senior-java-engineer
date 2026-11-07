package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * A booked trip: its booking reference and its legs, in order. A travel agent changes a booked
 * trip by editing an amendment of it; the amendment replaces the booking only once the customer
 * confirms it.
 */
public class Itinerary {

    /** One flight of the trip. The seat and the extra bags can change after booking. */
    public static class Leg {
        private final String flight;
        private String seat;
        private final List<String> bags;

        public Leg(String flight, String seat, List<String> bags) {
            this.flight = flight;
            this.seat = seat;
            this.bags = bags;
        }

        public String flight() {
            return flight;
        }

        public String seat() {
            return seat;
        }

        public void changeSeat(String seat) {
            this.seat = seat;
        }

        /** The leg's bags; add to this list to add a bag. */
        public List<String> bags() {
            return bags;
        }

        /** A leg like this one, for an amendment to change. */
        Leg copy() {
            return new Leg(flight, seat, bags);
        }

        @Override
        public String toString() {
            return flight + " " + seat + " " + bags;
        }
    }

    private final String reference;
    private final List<Leg> legs;

    public Itinerary(String reference, List<Leg> legs) {
        this.reference = reference;
        this.legs = legs;
    }

    public String reference() {
        return reference;
    }

    /** The trip's legs, in order; add or remove legs through this list. */
    public List<Leg> legs() {
        return legs;
    }

    /** An itinerary for the agent to edit: legs added or removed, seats changed, bags added. */
    public Itinerary amendment() {
        List<Leg> copied = new ArrayList<>();
        for (Leg leg : legs) copied.add(leg.copy());
        return new Itinerary(reference, copied);
    }

    @Override
    public String toString() {
        return reference + " " + legs;
    }
}
