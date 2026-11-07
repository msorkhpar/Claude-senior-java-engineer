package practice;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShippingTest {

    /** A carrier the freight team adds later: its own subclass, with its own constructor. */
    static class Freight extends Shipping.CachedRateCard {
        private final Map<String, Long> table;
        int lookUps;

        Freight(String currency, Map<String, Long> table) {
            super(currency);
            this.table = table;
        }

        @Override
        protected long lookUp(String zone) {
            lookUps++;
            return table.get(zone);
        }
    }

    @Test
    void parcelPostQuotesFromItsTableInAnIsoCurrency() {
        Shipping.RateCard post = new Shipping.ParcelPost("EUR", Map.of("pp-north", 790L, "pp-south", 1290L));

        assertThat(post.rateCents("pp-north")).isEqualTo(790L);
        assertThat(post.rateCents("pp-south")).isEqualTo(1290L);
        assertThat(post.currency()).isEqualTo("EUR");
        assertThatThrownBy(() -> new Shipping.ParcelPost("eur", Map.of("pp-east", 500L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Shipping.ParcelPost("EURO", Map.of("pp-east", 500L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aCarrierLooksEachZoneUpOnce() {
        Freight freight = new Freight("CHF", Map.of("fr-alps", 4200L, "fr-lake", 3100L));

        assertThat(freight.rateCents("fr-alps")).isEqualTo(4200L);
        assertThat(freight.rateCents("fr-lake")).isEqualTo(3100L);
        assertThat(freight.rateCents("fr-alps")).isEqualTo(4200L);
        assertThat(freight.rateCents("fr-alps")).isEqualTo(4200L);
        assertThat(freight.lookUps).isEqualTo(2);
        assertThat(freight.currency()).isEqualTo("CHF");
    }

    @Test
    void aCarrierWrittenLaterCantHaveALowerCaseCurrency() {
        assertThatThrownBy(() -> new Freight("usd", Map.of("fr-coast", 2500L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Freight("U$D", Map.of("fr-coast", 2500L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void twoCarriersKeepTheirOwnRatesForTheSameZone() {
        Shipping.RateCard post = new Shipping.ParcelPost("GBP", Map.of("island-7", 1450L));
        Freight freight = new Freight("GBP", Map.of("island-7", 2300L));

        assertThat(post.rateCents("island-7")).isEqualTo(1450L);
        assertThat(freight.rateCents("island-7")).isEqualTo(2300L);
        assertThat(freight.lookUps).isEqualTo(1);
    }
}
