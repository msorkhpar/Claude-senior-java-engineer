package practice;

import java.util.List;
import java.util.Optional;

/**
 * Assigns delivery jobs to couriers. Whether a courier can take a job right now is a question
 * for the courier service, and every question is one request against its rate limit.
 */
public class Dispatcher {

    public record Courier(String name, String zone) {}

    /** Asks the courier service whether a courier is free; one request per call. */
    public interface Availability {
        boolean isFree(Courier courier);
    }

    private final Availability availability;

    public Dispatcher(Availability availability) {
        this.availability = availability;
    }

    /** Names the courier who gets the job, or "queued" when nobody nearby is free. */
    public String assign(List<Courier> byDistance) {
        return nearestFree(byDistance).map(Courier::name).orElse("queued");
    }

    /** Whether the shop page may show "Delivery within the hour" for this zone. */
    public boolean canPromiseFastDelivery(List<Courier> inZone) {
        return anyFree(inZone);
    }

    /** The first free courier in the list, which is sorted nearest first. */
    Optional<Courier> nearestFree(List<Courier> byDistance) {
        // TODO: Return the nearest courier the service says is free. Every call to
        //       availability.isFree(...) is a request the service counts against our limit.
        throw new IllegalStateException("TODO: nearestFree");
    }

    /** Whether at least one courier in the list is free. */
    boolean anyFree(List<Courier> couriers) {
        // TODO: Answer whether anyone in the list is free, with as few requests to the
        //       service as the answer needs.
        throw new IllegalStateException("TODO: anyFree");
    }
}
