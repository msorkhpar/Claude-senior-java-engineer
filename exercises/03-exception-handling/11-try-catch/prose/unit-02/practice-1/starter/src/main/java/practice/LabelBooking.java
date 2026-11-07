package practice;

import java.util.Locale;

/** Books shipping labels with a carrier, and fixes up an address the carrier rejects. */
public class LabelBooking {

    /** Anything the carrier's API reports. Unchecked, as in most HTTP client libraries. */
    public static class CarrierException extends RuntimeException {
        public CarrierException(String message) {
            super(message);
        }
    }

    /** The carrier could not match the address to a delivery point. */
    public static class AddressRejected extends CarrierException {
        public AddressRejected(String reason) {
            super(reason);
        }
    }

    /** The carrier's booking API. */
    public interface Carrier {
        /** Books a label for the parcel and returns its tracking number. */
        String book(String parcel, String address);
    }

    private final Carrier carrier;

    public LabelBooking(Carrier carrier) {
        this.carrier = carrier;
    }

    /**
     * Books a label. Returns {@code "booked: <tracking number>"}, {@code "review: <reason>"} when the
     * address must be fixed by a person, or {@code "retry later: <message>"} when the carrier failed.
     */
    public String ship(String parcel, String address) {
        try {
            return "booked: " + carrier.book(parcel, address);
        } catch (AddressRejected e) {
            return bookNormalized(parcel, address);
        } catch (CarrierException e) {
            return "retry later: " + e.getMessage();
        }
    }

    /**
     * The carrier rejected the address as typed. Books the parcel once more with the address
     * normalized, and returns what ship() returns.
     */
    String bookNormalized(String parcel, String address) {
        // TODO: the parcel still has to be booked; decide what each outcome of the second attempt
        // means for ship()'s caller.
        throw new IllegalStateException("TODO: book the parcel with the normalized address");
    }

    /** Collapses runs of spaces and upper-cases the address, as the carrier's own form does. */
    static String normalize(String address) {
        return address.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }
}
