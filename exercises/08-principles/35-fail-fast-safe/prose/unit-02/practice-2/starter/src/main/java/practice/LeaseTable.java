package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hands out time-limited leases on shared resources (a build agent, a GPU, a printer). Holders
 * renew their lease before it runs out and release it when they are done; a sweeper thread frees
 * the leases nobody renewed. Holders renew and release on their own threads, at any time.
 */
public class LeaseTable {

    /** A lease is never changed: renewing it puts a new Lease in its place. */
    public record Lease(String resource, String holder, long expiresAt) {}

    /** Tells a holder that its lease ran out and the resource was given back. */
    public interface Notifier {
        void expired(Lease lease);
    }

    private final ConcurrentHashMap<String, Lease> leases;

    public LeaseTable() {
        this(new ConcurrentHashMap<>());
    }

    LeaseTable(ConcurrentHashMap<String, Lease> store) {
        this.leases = store;
    }

    /** Takes the resource until {@code expiresAt}, if nobody holds it. */
    public boolean acquire(String resource, String holder, long expiresAt) {
        return leases.putIfAbsent(resource, new Lease(resource, holder, expiresAt)) == null;
    }

    /** Extends the holder's lease to {@code expiresAt}; false if the holder no longer has it. */
    public boolean renew(String resource, String holder, long expiresAt) {
        Lease renewed = leases.computeIfPresent(resource,
                (r, current) -> current.holder().equals(holder) ? new Lease(r, holder, expiresAt) : current);
        return renewed != null && renewed.holder().equals(holder) && renewed.expiresAt() == expiresAt;
    }

    /** Gives the resource back; the holder needs no notice. */
    public void release(String resource, String holder) {
        leases.computeIfPresent(resource, (r, current) -> current.holder().equals(holder) ? null : current);
    }

    /** Who holds the resource now, or null. */
    public Lease holderOf(String resource) {
        return leases.get(resource);
    }

    /**
     * Frees every lease that ran out at or before {@code now}, tells its holder, and returns the
     * resources it freed.
     */
    public List<String> sweep(long now, Notifier notifier) {
        // TODO: Free the leases that ran out, tell their holders, and return the resources
        //       freed. Holders renew and release on their own threads while the sweep runs,
        //       and a holder must not lose a lease it has just renewed.
        throw new IllegalStateException("TODO: sweep");
    }
}
