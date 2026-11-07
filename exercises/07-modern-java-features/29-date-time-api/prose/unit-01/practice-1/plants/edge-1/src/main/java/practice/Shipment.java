package practice;

import java.util.Date;

/** A parcel handed to the carrier. The dispatch moment comes from an old carrier API as a Date. */
public class Shipment {

    private final String trackingId;
    private final Date dispatchedAt;

    public Shipment(String trackingId, Date dispatchedAt) {
        this.trackingId = trackingId;
        this.dispatchedAt = keep(dispatchedAt);
    }

    public String trackingId() {
        return trackingId;
    }

    /** The moment the parcel left the warehouse. */
    public Date dispatchedAt() {
        return handOut(dispatchedAt);
    }

    /** True when the parcel left the warehouse before the given cut-off. */
    public boolean dispatchedBefore(Date cutoff) {
        return dispatchedAt.before(cutoff);
    }

    // TODO: called once by the constructor with the caller's Date. Return what the shipment stores,
    // so that nothing the caller does to its own Date later changes this shipment.
    Date keep(Date fromCaller) {
        return fromCaller;
    }

    // TODO: called by dispatchedAt() with the stored Date. Return what the caller receives,
    // so that nothing the caller does to the returned Date changes this shipment.
    Date handOut(Date stored) {
        return new Date(stored.getTime());
    }
}
