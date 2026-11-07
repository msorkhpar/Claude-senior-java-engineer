package practice;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The payment reminder sent for an unpaid invoice. A copy of every letter also goes to the payment
 * portal, which reads the invoice line.
 */
public final class ReminderLetter {

    /** One unpaid invoice. {@code note} is free text from the account manager, or null. */
    public record Invoice(String customer, String number, BigDecimal amount, LocalDate due, String note) {
    }

    static final String LETTER = """
            Dear %s,
            invoice %s over EUR %.2f is due on %s.
            """;

    private ReminderLetter() {
    }

    /**
     * The letter for {@code invoice}: the two lines of {@link #LETTER}, then the account manager's
     * note as a third line when there is one. Every line ends with a line break.
     */
    public static String render(Invoice invoice) {
        // TODO: Fill in LETTER for this invoice and add the note. The portal reads the amount as
        //       digits, a dot and two decimals, on every server the job runs on.
        throw new IllegalStateException("TODO: render");
    }
}
