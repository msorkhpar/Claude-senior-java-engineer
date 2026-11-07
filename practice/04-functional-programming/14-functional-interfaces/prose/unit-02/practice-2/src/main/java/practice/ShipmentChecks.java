package practice;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Checks shipments on their way to the label printer. It runs for every shipment the warehouse
 * scans, and almost every shipment passes.
 */
public class ShipmentChecks {

    public record Shipment(String id, String carrier, List<String> parcels) {}

    private final Map<String, Shipment> byId;
    private final Function<Shipment, String> describe;          // formats every parcel: slow on big shipments
    private final Function<String, RuntimeException> notFound;  // builds the error for an unknown id

    public ShipmentChecks(Map<String, Shipment> byId, Function<Shipment, String> describe,
                          Function<String, RuntimeException> notFound) {
        this.byId = byId;
        this.describe = describe;
        this.notFound = notFound;
    }

    /** The shipment's carrier. A shipment without one fails with a NullPointerException whose message describes it. */
    public String carrierOf(Shipment shipment) {
        // TODO: Return the carrier. When it is missing, fail with a NullPointerException whose
        //       message is the shipment's description, without slowing down the shipments that pass.
        throw new IllegalStateException("TODO: carrierOf");
    }

    /** The shipment with this id. An unknown id fails with the exception notFound builds for it. */
    public Shipment find(String id) {
        // TODO: Return the shipment. When the id is unknown, throw what notFound builds for it,
        //       without slowing down the lookups that succeed.
        throw new IllegalStateException("TODO: find");
    }
}
