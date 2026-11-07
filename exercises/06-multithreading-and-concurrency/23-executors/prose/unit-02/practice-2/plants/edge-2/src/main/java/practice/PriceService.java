package practice;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The price shown on a product page. The supplier is asked on a shared pool; when it can't give a
 * price in time, the page shows the last price we cached for the product instead.
 */
public class PriceService {

    /** The supplier's pricing API. A call can take as long as the supplier likes. */
    public interface Supplier {
        long price(String sku) throws Exception;
    }

    private final ExecutorService pool;
    private final Supplier supplier;
    private final Map<String, Long> cached;
    private final long budgetMillis;

    public PriceService(ExecutorService pool, Supplier supplier, Map<String, Long> cached, long budgetMillis) {
        this.pool = pool;
        this.supplier = supplier;
        this.cached = cached;
        this.budgetMillis = budgetMillis;
    }

    /** The price to show for the product, in cents. */
    public long priceOf(String sku) throws InterruptedException {
        Future<Long> call = pool.submit(() -> supplier.price(sku));
        return priceOrCached(sku, call);
    }

    /**
     * The supplier's price if it arrives within the page's budget of budgetMillis, otherwise the
     * cached price. A supplier call that fails also falls back to the cached price.
     */
    long priceOrCached(String sku, Future<Long> call) throws InterruptedException {
        try {
            return call.get(budgetMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            call.cancel(false);
            return cached.get(sku);
        } catch (ExecutionException e) {
            return cached.get(sku);
        }
    }
}
