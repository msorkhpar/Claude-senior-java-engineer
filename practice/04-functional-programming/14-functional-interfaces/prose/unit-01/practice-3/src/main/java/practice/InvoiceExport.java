package practice;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Writes the month's invoices to the accounting file. Rendering one invoice takes a while and is
 * safe to run on several threads; the file writer is not.
 */
public final class InvoiceExport {

    private InvoiceExport() {
    }

    /**
     * Renders every invoice and hands the lines to the writer.
     *
     * @param invoiceIds the invoices, in the order the accounting file must list them
     * @param render     renders one invoice to its line; slow, thread-safe
     * @param writer     appends one line to the file; not thread-safe
     */
    public static void export(List<String> invoiceIds, Function<String, String> render, Consumer<String> writer) {
        // TODO: The month has tens of thousands of invoices, so render them in parallel. Every
        //       line reaches the file, in the order of invoiceIds, and the writer is never used
        //       by two threads at once.
        throw new IllegalStateException("TODO: export");
    }
}
