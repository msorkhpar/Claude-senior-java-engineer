package practice;

import java.util.Optional;
import java.util.function.Function;

/**
 * Sends each parcel in the depot to a sorting bin by the first two characters of its postcode.
 * Parcels the depot can't route yet (no recipient on file, an address still being confirmed, a
 * postcode not entered) go to the HOLD shelf for a person to look at.
 */
public class Sorting {

    public record Address(String street, String postcode) {}
    public record Recipient(String name, Address address) {}
    public record Parcel(String id, Recipient recipient) {}

    static final Function<Parcel, Recipient> RECIPIENT = Parcel::recipient;
    static final Function<Recipient, Address> ADDRESS = Recipient::address;
    static final Function<Address, String> POSTCODE = Address::postcode;

    /** The bin a parcel goes to. */
    public static String binFor(Parcel parcel) {
        return postcodeOf(parcel).map(postcode -> postcode.substring(0, 2)).orElse("HOLD");
    }

    /** The parcel's postcode, or empty when any part of the way to it is missing. */
    static Optional<String> postcodeOf(Parcel parcel) {
        if (parcel == null) return Optional.empty();
        return Optional.ofNullable(RECIPIENT.andThen(ADDRESS).andThen(POSTCODE).apply(parcel));
    }
}
