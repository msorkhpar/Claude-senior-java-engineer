package practice;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * One connection pool per tenant of a reporting service. Request threads ask for their tenant's
 * pool on every request; an admin action or a failed health check discards a pool, and the next
 * request opens a fresh one.
 */
public class TenantPools {

    /** A tenant's pool. Opening one connects to the tenant's database, which takes a while. */
    public static final class Pool {
        private final String tenant;

        public Pool(String tenant) {
            this.tenant = tenant;
        }

        public String tenant() {
            return tenant;
        }
    }

    private final ConcurrentHashMap<String, Pool> pools = new ConcurrentHashMap<>();
    private final Function<String, Pool> opener;

    /** opener opens a new pool for a tenant. */
    public TenantPools(Function<String, Pool> opener) {
        this.opener = opener;
    }

    /**
     * Returns the tenant's pool, opening it if the tenant has none. Every caller asking for the
     * same tenant gets the same pool, and a tenant's pool is opened once, not once per request.
     */
    public Pool poolFor(String tenant) {
        return pools.computeIfAbsent(tenant, opener);
    }

    /**
     * Discards the given pool of the tenant, if it is still the tenant's pool. Returns whether
     * it was discarded. A caller holding a pool that was already replaced must not discard the
     * replacement.
     */
    public boolean discard(String tenant, Pool pool) {
        return pools.remove(tenant) != null;
    }

    public int size() {
        return pools.size();
    }
}
