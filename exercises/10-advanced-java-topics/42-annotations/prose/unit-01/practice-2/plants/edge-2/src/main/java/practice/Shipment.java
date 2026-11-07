package practice;

import java.util.Arrays;
import java.util.List;

/** The parcels that leave the warehouse together on one truck. */
public final class Shipment<T> {
    private final List<T> parcels;

    private Shipment(List<T> parcels) {
        this.parcels = parcels;
    }

    /**
     * Builds a shipment from its parcels. Generic code calls this too, for example a helper that
     * ships one parcel of any type, and the build compiles with -Xlint:unchecked -Werror.
     */
    @SafeVarargs
    public static <T> Shipment<T> of(T... parcels) {
        return new Shipment<>(Arrays.asList(parcels));
    }

    public List<T> parcels() {
        return parcels;
    }

    public int size() {
        return parcels.size();
    }
}
