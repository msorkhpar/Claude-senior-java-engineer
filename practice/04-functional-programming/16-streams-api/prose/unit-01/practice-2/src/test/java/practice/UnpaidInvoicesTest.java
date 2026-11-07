package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnpaidInvoicesTest {

    private static List<UnpaidInvoices.Invoice> ledger() {
        return new ArrayList<>(List.of(
                new UnpaidInvoices.Invoice("INV-2041", 12_500, false),
                new UnpaidInvoices.Invoice("INV-2042", 3_099, true),
                new UnpaidInvoices.Invoice("INV-2043", 780, false),
                new UnpaidInvoices.Invoice("INV-2044", 45_000, true),
                new UnpaidInvoices.Invoice("INV-2045", 1_001, false)));
    }

    @Test
    void countsTheUnpaidInvoices() {
        assertThat(new UnpaidInvoices(ledger()).count()).isEqualTo(3);
    }

    @Test
    void addsUpTheUnpaidAmount() {
        assertThat(new UnpaidInvoices(ledger()).totalCents()).isEqualTo(14_281);
    }

    @Test
    void listsTheUnpaidIdsInLedgerOrder() {
        assertThat(new UnpaidInvoices(ledger()).ids()).containsExactly("INV-2041", "INV-2043", "INV-2045");
    }

    @Test
    void anOpenPanelAnswersEveryRefresh() {
        UnpaidInvoices panel = new UnpaidInvoices(ledger());

        assertThat(panel.count()).isEqualTo(3);
        assertThat(panel.totalCents()).isEqualTo(14_281);
        assertThat(panel.ids()).containsExactly("INV-2041", "INV-2043", "INV-2045");
        assertThat(panel.count()).isEqualTo(3);
    }

    @Test
    void anInvoiceAddedWhileThePanelIsOpenShowsOnTheNextRefresh() {
        List<UnpaidInvoices.Invoice> ledger = ledger();
        UnpaidInvoices panel = new UnpaidInvoices(ledger);

        ledger.add(new UnpaidInvoices.Invoice("INV-2046", 219, false));

        assertThat(panel.ids()).containsExactly("INV-2041", "INV-2043", "INV-2045", "INV-2046");
    }
}
