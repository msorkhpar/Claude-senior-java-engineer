package practice;

import java.util.List;

/**
 * The end-of-day reconciliation asks the bank for the status of every payment of the day. Each
 * lookup blocks until the bank answers, which can take seconds. The same service answers card
 * authorisations whose parallel streams and async tasks run on the JVM's common pool.
 */
public class StatusReport {

    /** Asks the bank for one payment's status. Blocks until the bank answers; may throw. */
    public interface BankClient {
        String status(String paymentId);
    }

    private final BankClient bank;
    private final int lookupsAtOnce;

    public StatusReport(BankClient bank, int lookupsAtOnce) {
        this.bank = bank;
        this.lookupsAtOnce = lookupsAtOnce;
    }

    /** The status of every payment, in the order of the ids. */
    public List<String> statuses(List<String> paymentIds) {
        // TODO: The lookups block for seconds at a time, and while they do, the card authorisations
        //       must not lose any of the threads they rely on. Run lookupsAtOnce lookups at a time
        //       on threads of the report's own, return the statuses in the order of the ids, and
        //       leave no threads behind. A lookup that fails reaches the caller as the same kind
        //       of exception.
        throw new IllegalStateException("TODO: statuses");
    }
}
