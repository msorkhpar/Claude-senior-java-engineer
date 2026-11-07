package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Matches the payments on a bank statement to open invoices by the reference the payer typed.
 * Whatever it can't match goes to a person for manual review.
 */
public class Reconciler {

    /** An open invoice. Its reference is null when it was issued without one. */
    public record Invoice(String number, String reference) { }

    /** A payment from the bank statement. Its reference is null when the payer left it empty. */
    public record Payment(String id, String reference) { }

    /**
     * Reads the bank's statement file: one payment per line, as {@code id;reference}, with
     * nothing after the semicolon when the payer gave no reference.
     */
    public static List<Payment> readStatement(String file) {
        List<Payment> payments = new ArrayList<>();
        for (String line : file.split("\n")) {
            if (line.isBlank()) continue;
            String[] fields = line.split(";", -1);
            String reference = fields[1].strip();
            payments.add(new Payment(fields[0].strip(), reference.isEmpty() ? null : reference));
        }
        return payments;
    }

    /** For every payment it could match, the number of the invoice it pays, keyed by payment id. */
    public Map<String, String> match(List<Invoice> invoices, List<Payment> payments) {
        Map<String, String> matched = new LinkedHashMap<>();
        for (Payment payment : payments) {
            for (Invoice invoice : invoices) {
                if (sameReference(invoice.reference(), payment.reference())) {
                    matched.put(payment.id(), invoice.number());
                    break;
                }
            }
        }
        return matched;
    }

    /** Whether a payment carrying {@code paymentRef} pays the invoice issued with {@code invoiceRef}. */
    boolean sameReference(String invoiceRef, String paymentRef) {
        return invoiceRef == paymentRef;
    }
}
