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
        return Objects.requireNonNull(shipment.carrier(), () -> describe.apply(shipment));
    }

    /** The shipment with this id. An unknown id fails with the exception notFound builds for it. */
    public Shipment find(String id) {
        return Optional.ofNullable(byId.get(id)).orElseThrow(() -> notFound.apply(id));
    }
}
