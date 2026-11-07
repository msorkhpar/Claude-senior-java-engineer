package practice;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Maps postcode districts to delivery regions. The table comes from the address service, which
 * takes a few seconds to answer, so it is fetched on first use rather than at startup. Checkout
 * threads call region(...) on every order.
 */
public class PostcodeLookup {

    private final Supplier<Map<String, String>> loader;

    public PostcodeLookup(Supplier<Map<String, String>> loader) {
        this.loader = loader;
    }

    /** The delivery region of a postcode district, or null for one the table doesn't know. */
    public String region(String district) {
        return table().get(district);
    }

    /** The whole table, fetched with the loader on the first call. */
    public Map<String, String> table() {
        // TODO: Fetch the table with the loader the first time it is needed, and hand the same
        //       table to every caller after that. Checkout threads call this concurrently, and
        //       the address service sometimes times out. Add any fields you need.
        throw new IllegalStateException("TODO: table");
    }
}
