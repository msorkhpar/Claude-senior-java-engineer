package practice;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * Adds up the tax on a batch of invoices for the monthly export. The tax service answers one
 * invoice per call over HTTP, so the lookups run in parallel, and each one can wait seconds.
 */
public class InvoiceExport {

    /** Looks up the tax of one invoice, in cents; a remote call that may block. */
    public interface TaxService {
        long taxFor(String invoice);
    }

    private final TaxService taxService;
    private final int lookupThreads;

    public InvoiceExport(TaxService taxService, int lookupThreads) {
        this.taxService = taxService;
        this.lookupThreads = lookupThreads;
    }

    /** The total tax of the invoices, in cents. */
    public long totalTax(List<String> invoices) {
        return onLookupThreads(() -> invoices.parallelStream().mapToLong(taxService::taxFor).sum());
    }

    /** Runs the lookups and returns what they computed. */
    <T> T onLookupThreads(Supplier<T> lookups) {
        try (ForkJoinPool pool = new ForkJoinPool(lookupThreads)) {
            return pool.submit(lookups::get).join();
        }
    }
}
