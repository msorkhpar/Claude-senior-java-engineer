package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReconcilerTest {

    private final Reconciler reconciler = new Reconciler();

    private static final List<Reconciler.Invoice> OPEN = List.of(
            new Reconciler.Invoice("2024-0187", "RF18 5390 0754 7034"),
            new Reconciler.Invoice("2024-0188", "RF71 2240 1110 0918"),
            new Reconciler.Invoice("2024-0190", "RF02 6610 0007 1185"));

    @Test
    void aPaymentIsMatchedToTheInvoiceWithItsReference() {
        List<Reconciler.Payment> payments = List.of(
                new Reconciler.Payment("TX-55021", "RF71 2240 1110 0918"),
                new Reconciler.Payment("TX-55022", "RF99 0000 0000 0000"));

        Map<String, String> matched = reconciler.match(OPEN, payments);

        assertThat(matched).containsExactly(Map.entry("TX-55021", "2024-0188"));
    }

    @Test
    void paymentsReadFromTheBanksFileAreMatched() {
        String file = """
                TX-60310;RF02 6610 0007 1185
                TX-60311;RF18 5390 0754 7034
                TX-60312;RF55 1234 5678 9012
                """;

        Map<String, String> matched = reconciler.match(OPEN, Reconciler.readStatement(file));

        assertThat(matched).containsOnly(
                Map.entry("TX-60310", "2024-0190"),
                Map.entry("TX-60311", "2024-0187"));
    }

    @Test
    void anInvoiceIssuedWithoutAReferenceDoesNotStopTheRun() {
        List<Reconciler.Invoice> invoices = List.of(
                new Reconciler.Invoice("2024-0191", null),
                new Reconciler.Invoice("2024-0187", "RF18 5390 0754 7034"));
        List<Reconciler.Payment> payments = List.of(
                new Reconciler.Payment("TX-70001", "RF18 5390 0754 7034"));

        Map<String, String> matched = reconciler.match(invoices, payments);

        assertThat(matched).containsExactly(Map.entry("TX-70001", "2024-0187"));
    }

    @Test
    void aPaymentWithoutAReferenceGoesToManualReview() {
        List<Reconciler.Invoice> invoices = List.of(
                new Reconciler.Invoice("2024-0191", null),
                new Reconciler.Invoice("2024-0188", "RF71 2240 1110 0918"));
        List<Reconciler.Payment> payments = List.of(
                new Reconciler.Payment("TX-70002", null),
                new Reconciler.Payment("TX-70003", "RF71 2240 1110 0918"));

        Map<String, String> matched = reconciler.match(invoices, payments);

        assertThat(matched).containsExactly(Map.entry("TX-70003", "2024-0188"));
    }
}
