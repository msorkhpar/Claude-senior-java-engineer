package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** The checkout's view of customers and prices, over two slow back ends it counts calls to. */
public class Checkout {

    /** The customer table: email to customer id. */
    final Map<String, String> customers = new HashMap<>();
    /** How many customer rows have been inserted. */
    int inserts;

    /** The catalogue service's prices, in cents, and how many times it has been asked. */
    final Map<String, Long> catalogue;
    int catalogueCalls;

    /** Prices already fetched from the catalogue. */
    final Map<String, Long> priceCache = new HashMap<>();

    public Checkout(Map<String, Long> catalogue) {
        this.catalogue = catalogue;
    }

    Optional<String> findCustomer(String email) {
        return Optional.ofNullable(customers.get(email));
    }

    /** Inserts a customer row and returns its new id. */
    String insertCustomer(String email) {
        inserts++;
        String id = "cus-" + (1000 + inserts);
        customers.put(email, id);
        return id;
    }

    /** Asks the catalogue service for a price: slow, and billed per call. */
    long fetchPrice(String sku) {
        catalogueCalls++;
        Long cents = catalogue.get(sku);
        if (cents == null) throw new IllegalArgumentException("unknown SKU " + sku);
        return cents;
    }

    /** The id of the customer with this email; a first-time customer gets a new row. */
    public String customerId(String email) {
        return findCustomer(email).orElseGet(() -> insertCustomer(email));
    }

    /** The price of a SKU in cents; the catalogue is asked once per SKU, then the cache answers. */
    public long price(String sku) {
        long cents = priceCache.getOrDefault(sku, fetchPrice(sku));
        priceCache.put(sku, cents);
        return cents;
    }
}
