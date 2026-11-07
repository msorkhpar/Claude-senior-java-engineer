package practice;

/**
 * The depot's shipment journal. Scanner worker threads call record() for every parcel event, all
 * at once, and a flusher copies contents() to disk every few seconds.
 */
public class ShipmentJournal {

    /** One scanned parcel. Its getters can be slow: depot() looks the depot up in another service. */
    public interface Event {
        String parcel();

        String depot();

        int grams();
    }

    private final StringBuffer out = new StringBuffer();

    /** Writes the event's line, {@code parcel=<parcel> depot=<depot> grams=<grams>} and a newline. */
    public void record(Event e) {
        synchronized (out) {
            out.append("parcel=").append(e.parcel()).append(" depot=").append(e.depot())
                    .append(" grams=").append(e.grams()).append('\n');
        }
    }

    /** Everything recorded so far. */
    public String contents() {
        return out.toString();
    }
}
