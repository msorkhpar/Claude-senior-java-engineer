package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockLedgerTest {

    @Test
    void reportsTheReceivedUnitsAndFindsTheUnevenPairs() {
        StockLedger ledger = new StockLedger();
        ledger.receive("boot-42-L", 12);
        ledger.receive("boot-42-R", 9);
        ledger.receive("boot-42-R", 3);
        ledger.receive("clog-38-L", 5);
        ledger.receive("clog-38-R", 4);

        assertThat(ledger.report(List.of("boot-42-L", "clog-38-R"))).isEqualTo("boot-42-L: 12\nclog-38-R: 4");
        assertThat(ledger.unevenPairs(List.of("boot-42-L", "clog-38-L"), List.of("boot-42-R", "clog-38-R")))
                .containsExactly("clog-38-L/clog-38-R");
    }

    @Test
    void aSkuNeverReceivedHasNoUnits() {
        StockLedger ledger = new StockLedger();
        ledger.receive("boot-42-L", 12);

        assertThat(ledger.report(List.of("boot-42-L", "sandal-40-L"))).isEqualTo("boot-42-L: 12\nsandal-40-L: 0");
    }

    @Test
    void largeEqualCountsArePaired() {
        StockLedger ledger = new StockLedger();
        ledger.receive("trainer-44-L", 1_200);
        ledger.receive("trainer-44-L", 1_300);
        ledger.receive("trainer-44-R", 2_000);
        ledger.receive("trainer-44-R", 500);

        assertThat(ledger.unevenPairs(List.of("trainer-44-L"), List.of("trainer-44-R"))).isEmpty();
    }

    @Test
    void anEmptySkuAndAnUnknownOneHaveTheSameStock() {
        StockLedger ledger = new StockLedger();
        ledger.receive("slipper-39-L", 0);

        assertThat(ledger.unevenPairs(List.of("slipper-39-L"), List.of("slipper-39-R"))).isEmpty();
    }
}
