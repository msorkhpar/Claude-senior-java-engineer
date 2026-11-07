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
        Long old = get(sku);
        Long now = old == null ? cents : remap.apply(old, cents);
        if (now == null) remove(sku);
        else put(sku, now);
        return now;
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Long, ? extends Long> change) {
        super.replaceAll(change);
    }
}
