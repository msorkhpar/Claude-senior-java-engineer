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
        // TODO: Many scanner threads call this at the same time, and depot() can take a while.
        //       Every line in the journal must be one event's line, whole, and a slow event
        //       must not hold up the other threads' records.
        throw new IllegalStateException("TODO: record");
    }

    /** Everything recorded so far. */
    public String contents() {
        return out.toString();
    }
}
