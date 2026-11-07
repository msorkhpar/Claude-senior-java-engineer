package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class ReminderLetterTest {

    private Locale before;

    @BeforeEach
    void anEnglishSpeakingServer() {
        before = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    void restoreTheLocale() {
        Locale.setDefault(before);
    }

    private static ReminderLetter.Invoice invoice(String amount, String note) {
        return new ReminderLetter.Invoice("Example Logistics B.V.", "INV-2026-0418",
                new BigDecimal(amount), LocalDate.of(2026, 12, 15), note);
    }

    @Test
    void aLetterWithoutANote() {
        assertThat(ReminderLetter.render(invoice("980.40", null))).isEqualTo("""
                Dear Example Logistics B.V.,
                invoice INV-2026-0418 over EUR 980.40 is due on 2026-12-15.
                """);
        assertThat(ReminderLetter.render(invoice("1500", null)))
                .contains("invoice INV-2026-0418 over EUR 1500.00 is due on 2026-12-15.");
    }

    @Test
    void theNoteComesAfterTheInvoiceLine() {
        assertThat(ReminderLetter.render(invoice("12.05", "Please use the new bank account.")))
                .isEqualTo("""
                        Dear Example Logistics B.V.,
                        invoice INV-2026-0418 over EUR 12.05 is due on 2026-12-15.
                        Please use the new bank account.
                        """);
    }

    @Test
    void theAmountReadsTheSameOnAGermanServer() {
        Locale.setDefault(Locale.GERMANY);

        assertThat(ReminderLetter.render(invoice("3150.75", null)))
                .contains("invoice INV-2026-0418 over EUR 3150.75 is due on 2026-12-15.");
    }

    @Test
    void aNoteWithAPercentSignIsCopiedAsWritten() {
        assertThat(ReminderLetter.render(invoice("400.00", "Discount of 2% agreed by phone.")))
                .endsWith("is due on 2026-12-15.\nDiscount of 2% agreed by phone.\n");
    }
}
