package practice;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** Tells the customers of a region that an outage is planned, by every channel they gave us. */
public class OutageNotifier {

    /** A customer from the CRM. Either contact may be missing (null). */
    public record Customer(String name, String email, String phone, int[] quietHours) {}

    /** Sends the notice to every contact of the segment, except during a customer's quiet hours. */
    public int notify(List<Customer> segment, int hour, Consumer<String> send) {
        int sent = 0;
        for (Customer c : segment == null ? List.<Customer>of() : segment) {
            if (isQuietHour(c.quietHours(), hour)) continue;
            for (String contact : contacts(List.of(c))) {
                send.accept(contact);
                sent++;
            }
        }
        return sent;
    }

    /**
     * The contacts of a segment: each customer's email, then phone, customer by customer, leaving
     * out missing ones. The CRM returns null for a segment with no customers.
     */
    List<String> contacts(List<Customer> segment) {
        // TODO: Every email and phone of the segment, in order, with nothing missing in the list.
        throw new IllegalStateException("TODO: contacts");
    }

    /** Whether hour (0-23) is one of the customer's quiet hours. */
    boolean isQuietHour(int[] quietHours, int hour) {
        // TODO: Look the hour up in the customer's quiet hours.
        throw new IllegalStateException("TODO: isQuietHour");
    }
}
