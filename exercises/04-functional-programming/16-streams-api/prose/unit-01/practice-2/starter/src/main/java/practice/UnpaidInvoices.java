package practice;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The "unpaid" panel of the billing page. One panel is created when the page opens and answers
 * every refresh until it closes. The ledger it is given is the billing service's live list:
 * new invoices are appended to it while the panel is open.
 */
public class UnpaidInvoices {

    public record Invoice(String id, long cents, boolean paid) {}

    private final Supplier<Stream<Invoice>> unpaid;

    public UnpaidInvoices(List<Invoice> ledger) {
        this.unpaid = unpaidOf(ledger);
    }

    /** How many invoices are unpaid. */
    public long count() {
        return unpaid.get().count();
    }

    /** The unpaid amount, in cents. */
    public long totalCents() {
        return unpaid.get().mapToLong(Invoice::cents).sum();
    }

    /** The ids of the unpaid invoices, in ledger order. */
    public List<String> ids() {
        return unpaid.get().map(Invoice::id).toList();
    }

    /** Where the panel's answers come from: the ledger's unpaid invoices, in ledger order. */
    static Supplier<Stream<Invoice>> unpaidOf(List<Invoice> ledger) {
        // TODO: Every count(), totalCents() and ids() call, on every refresh of the panel, gets
        //       its unpaid invoices from what this returns.
        throw new IllegalStateException("TODO: unpaidOf");
    }
}
