package practice;

import java.util.ArrayList;
import java.util.List;

/** Prints the line that goes on the parcel for each order event the warehouse receives. */
public class ShippingLabels {

    public record Address(String city, String postcode) {}

    /** Either address may be null: some customers never gave one. */
    public record Customer(String name, Address billing, Address shipping) {}

    public record Order(String number, Customer customer) {}

    /** The labels for a day's events, in order. */
    public static List<String> labels(List<?> events) {
        List<String> lines = new ArrayList<>();
        for (Object event : events) lines.add(shipTo(event));
        return lines;
    }

    /** The label for a parcel sent to an address. */
    static String label(String number, String name, String city, String postcode) {
        return number + " " + name + ", " + postcode + " " + city;
    }

    /** The label for a parcel the customer collects at the counter. */
    static String pickup(String number, String name) {
        return number + " " + name + ", PICKUP";
    }

    /** The label for one event. */
    static String shipTo(Object event) {
        // TODO: For an Order, the parcel goes to the customer's shipping address, or to the
        //       billing address when there is no shipping address on file; with neither, the
        //       customer collects it (pickup). Anything that is not an Order is rejected with an
        //       IllegalArgumentException.
        throw new IllegalStateException("TODO: shipTo");
    }
}
