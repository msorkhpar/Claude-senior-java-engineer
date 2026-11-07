package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentImportTest {

    @Test
    void importsEveryGoodRowInOrder() {
        PaymentImport.Ledger ledger = new PaymentImport.Ledger();
        PaymentImport.Rows rows = PaymentImport.create(ledger);

        rows.importAll(List.of("NL91-0417,250.00", "BE68-5390,19.95", "FR14-2004,1200.10"));

        assertThat(ledger.entries()).containsExactly(
                "payment from NL91-0417", "amount 250.00",
                "payment from BE68-5390", "amount 19.95",
                "payment from FR14-2004", "amount 1200.10");
        assertThat(rows.rejected()).isEmpty();
    }

    @Test
    void aRowWithoutAnAmountIsRejectedAndTheOthersImported() {
        PaymentImport.Ledger ledger = new PaymentImport.Ledger();
        PaymentImport.Rows rows = PaymentImport.create(ledger);

        rows.importAll(List.of("DE44-5001,75.00", "AT61-1904,", "IT60-0542,310.40"));

        assertThat(rows.rejected()).containsExactly("AT61-1904,");
        assertThat(ledger.entries()).contains("amount 75.00", "amount 310.40");
    }

    @Test
    void eachRowCommitsOnItsOwn() {
        PaymentImport.Ledger ledger = new PaymentImport.Ledger();
        PaymentImport.Rows rows = PaymentImport.create(ledger);

        rows.importAll(List.of("ES91-2100,12.00", "PT50-0002,", "IE29-AIBK,88.80", "LU28-0019,5.25"));

        assertThat(ledger.commits()).isEqualTo(3);
        assertThat(ledger.rollbacks()).isEqualTo(1);
    }

    @Test
    void aRejectedRowLeavesNothingInTheLedger() {
        PaymentImport.Ledger ledger = new PaymentImport.Ledger();
        PaymentImport.Rows rows = PaymentImport.create(ledger);

        rows.importAll(List.of("SE45-5000,40.00", "DK50-0040,", "FI21-1234,61.10"));

        assertThat(ledger.entries()).containsExactly(
                "payment from SE45-5000", "amount 40.00",
                "payment from FI21-1234", "amount 61.10");
    }
}
