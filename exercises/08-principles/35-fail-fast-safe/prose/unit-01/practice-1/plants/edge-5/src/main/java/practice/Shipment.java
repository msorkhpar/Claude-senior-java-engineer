package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** A shipment as the warehouse accepts it from a carrier's feed. The dispatch run reads it hours later. */
public record Shipment(String trackingId, String destination, int parcels, List<String> labels) {

    public Shipment {
        requireFields(trackingId, destination, parcels);
        labels = labelsToKeep(labels);
    }

    /**
     * TODO: refuse, here, a shipment the dispatch run could not handle: no tracking id or a blank
     * one, no destination, or a parcel count outside 1..20.
     */
    static void requireFields(String trackingId, String destination, int parcels) {
        Objects.requireNonNull(trackingId, "trackingId");
        Objects.requireNonNull(destination, "destination");
        if (trackingId.isBlank())
            throw new IllegalArgumentException("trackingId is blank");
        if (parcels < 1 || parcels > 20)
            throw new IllegalArgumentException("parcels " + parcels + " is not in 1..20");
    }

    /**
     * TODO: the label list this shipment stores. Every label must be present, and the list must
     * stay as it was when the shipment was accepted.
     */
    static List<String> labelsToKeep(List<String> labels) {
        return Collections.unmodifiableList(new ArrayList<>(labels));
    }

    /** The line the dispatch run prints on the pallet sheet. */
    public String routingCode() {
        return destination.toUpperCase(Locale.ROOT) + "/" + trackingId + "/" + parcels;
    }
}
