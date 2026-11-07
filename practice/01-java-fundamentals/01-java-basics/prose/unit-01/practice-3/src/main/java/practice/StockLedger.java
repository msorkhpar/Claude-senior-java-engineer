package practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Counts the units a warehouse has on hand per SKU. Shoes are stocked as two SKUs, one per foot,
 * and the pairing report flags every pair whose two counts differ.
 */
public class StockLedger {

    private final Map<String, Integer> units = new HashMap<>();

    /** Books {@code quantity} received units of a SKU; a quantity of 0 registers the SKU. */
    public void receive(String sku, int quantity) {
        units.merge(sku, quantity, Integer::sum);
    }

    /** One line per SKU, "sku: count", in the order asked for. */
    public String report(List<String> skus) {
        return skus.stream().map(sku -> sku + ": " + onHand(sku)).collect(Collectors.joining("\n"));
    }

    /** The pairs, as "left/right", whose two counts differ. */
    public List<String> unevenPairs(List<String> lefts, List<String> rights) {
        return IntStream.range(0, lefts.size())
                .filter(i -> !sameStock(lefts.get(i), rights.get(i)))
                .mapToObj(i -> lefts.get(i) + "/" + rights.get(i))
                .toList();
    }

    /** Units on hand; a SKU the warehouse never received has none. */
    int onHand(String sku) {
        // TODO: Answer from the units map. The report and the pairing check ask about SKUs the
        //       warehouse has never received, too.
        throw new IllegalStateException("TODO: onHand");
    }

    /** Whether the two SKUs have the same number of units on hand. */
    boolean sameStock(String a, String b) {
        // TODO: Compare the two SKUs' counts. Some warehouses hold thousands of each.
        throw new IllegalStateException("TODO: sameStock");
    }
}
