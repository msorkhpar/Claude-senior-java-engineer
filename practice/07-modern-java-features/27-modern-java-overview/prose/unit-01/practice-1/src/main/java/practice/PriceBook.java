package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Prices in cents by SKU. Finance audits every change, so each one is written to the trail as
 * "sku: old -> new" (old is "none" for a new SKU, new is "removed" for a deleted one).
 * Written years ago; recently the adjustment job started calling merge(), and the yearly price
 * increase calls replaceAll().
 */
public class PriceBook extends HashMap<String, Long> {

    private final List<String> trail = new ArrayList<>();

    /** The audit trail, oldest change first. */
    public List<String> trail() {
        return List.copyOf(trail);
    }

    private void record(String sku, Long old, Object now) {
        trail.add(sku + ": " + (old == null ? "none" : old) + " -> " + now);
    }

    @Override
    public Long put(String sku, Long cents) {
        Long old = super.put(sku, cents);
        record(sku, old, cents);
        return old;
    }

    @Override
    public Long remove(Object sku) {
        boolean had = containsKey(sku);
        Long old = super.remove(sku);
        if (had) record((String) sku, old, "removed");
        return old;
    }

    @Override
    public Long merge(String sku, Long cents, BiFunction<? super Long, ? super Long, ? extends Long> remap) {
        // TODO: The adjustment job calls merge(), and its changes never reach the trail. Make
        //       every change merge() makes appear in the trail, and keep what merge() promises
        //       to its callers.
        throw new IllegalStateException("TODO: merge");
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Long, ? extends Long> change) {
        // TODO: The yearly increase calls replaceAll(), and its changes never reach the trail.
        //       Make every price it changes appear in the trail.
        throw new IllegalStateException("TODO: replaceAll");
    }
}
