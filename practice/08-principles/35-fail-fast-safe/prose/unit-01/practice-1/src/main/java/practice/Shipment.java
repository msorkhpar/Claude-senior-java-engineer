package practice;

import java.util.List;
import java.util.Locale;

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
        throw new IllegalStateException("TODO: refuse a shipment the dispatch run could not handle");
    }

    /**
     * TODO: the label list this shipment stores. Every label must be present, and the list must
     * stay as it was when the shipment was accepted.
     */
    static List<String> labelsToKeep(List<String> labels) {
        throw new IllegalStateException("TODO: the labels this shipment keeps");
    }

    /** The line the dispatch run prints on the pallet sheet. */
    public String routingCode() {
        return destination.toUpperCase(Locale.ROOT) + "/" + trackingId + "/" + parcels;
    }
}
