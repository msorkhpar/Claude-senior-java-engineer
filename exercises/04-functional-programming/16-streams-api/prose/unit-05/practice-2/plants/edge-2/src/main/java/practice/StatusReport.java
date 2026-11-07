package practice;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

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
        ForkJoinPool lookups = new ForkJoinPool(lookupsAtOnce);
        try {
            return lookups.submit(() -> paymentIds.stream().map(bank::status).toList()).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting for the bank", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException failure) throw failure;
            if (e.getCause() instanceof Error error) throw error;
            throw new IllegalStateException(e.getCause());
        } finally {
            lookups.shutdown();
        }
    }
}
