package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.function.LongFunction;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerTest {

    @Test
    void everyEntryIdKeepsItsIdAndGainsThePeriod() {
        Function<String, String> march = Ledger.withPeriod("/2031-03");

        for (String id : List.of("AP-9", "GL-40417")) {
            String exported = march.apply(id);
            assertThat(exported).contains(id).contains("/2031-03");
            assertThat(exported).hasSize(id.length() + "/2031-03".length());
        }
    }

    @Test
    void bothColumnsShowTheAmount() {
        Ledger.FlaggedFormat format = new Ledger.FlaggedFormat(1_000_000);

        assertThat(format.exportColumn().apply(3_250_008)).contains("32500.08");
        assertThat(format.screenColumn().apply(3_250_008)).contains("32500.08");
        assertThat(format.exportColumn().apply(-45)).contains("-0.45");
        assertThat(format.screenColumn().apply(-45)).contains("-0.45");
    }

    @Test
    void thePeriodComesAfterTheEntryId() {
        Function<String, String> march = Ledger.withPeriod("/2031-03");

        assertThat(march.apply("AP-9")).isEqualTo("AP-9/2031-03");
        assertThat(march.apply("GL-40417")).isEqualTo("GL-40417/2031-03");
    }

    @Test
    void theExportColumnHoldsTheBareAmountEvenAboveTheLimit() {
        Ledger.FlaggedFormat format = new Ledger.FlaggedFormat(1_000_000);

        assertThat(format.exportColumn().apply(3_250_008)).isEqualTo("32500.08");
        assertThat(format.exportColumn().apply(-1_500_000)).isEqualTo("-15000.00");
    }

    @Test
    void theScreenFollowsAFormatThatExtendsTheFlaggedOne() {
        Ledger.FlaggedFormat highlighted = new Ledger.FlaggedFormat(1_000_000) {
            @Override
            public String render(long cents) {
                return "<b>" + super.render(cents) + "</b>";
            }
        };
        LongFunction<String> screen = highlighted.screenColumn();

        assertThat(screen.apply(3_250_008)).isEqualTo("<b>32500.08 [review]</b>");
        assertThat(screen.apply(990)).isEqualTo("<b>9.90</b>");
    }
}
