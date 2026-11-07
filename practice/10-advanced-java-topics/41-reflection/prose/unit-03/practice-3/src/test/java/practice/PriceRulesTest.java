package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceRulesTest {

    public static class Seasonal {
        public long winterSale(long cents) { return cents * 85 / 100; }

        public long clearance(long cents) { return cents / 2; }

        /** Written by someone who thought prices fit in an int. */
        public int wholeEuros(long cents) { return (int) (cents / 100 * 100); }
    }

    public static class Loyalty {
        private final long bonus = 250;

        public long goldMember(long cents) { return Math.max(0, cents - bonus); }
    }

    @Test
    void appliesTheNamedRuleToEveryPrice() {
        PriceRules.Rule winter = PriceRules.bind(Seasonal.class, "winterSale");
        PriceRules.Rule clearance = PriceRules.bind(Seasonal.class, "clearance");

        assertThat(winter.apply(12_000)).isEqualTo(10_200);
        assertThat(winter.apply(4_000_000_000L)).isEqualTo(3_400_000_000L);
        assertThat(clearance.apply(999)).isEqualTo(499);
    }

    @Test
    void appliesARuleThatUsesItsOwnState() {
        PriceRules.Rule gold = PriceRules.bind(Loyalty.class, "goldMember");

        assertThat(gold.apply(1_000)).isEqualTo(750);
        assertThat(gold.apply(100)).isEqualTo(0);
    }

    @Test
    void aMethodThatDoesNotExistIsRefusedWhenBound() {
        assertThatThrownBy(() -> PriceRules.bind(Seasonal.class, "springSale"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("springSale");
    }

    @Test
    void aMethodThatReturnsAnotherTypeIsRefusedWhenBound() {
        assertThatThrownBy(() -> PriceRules.bind(Seasonal.class, "wholeEuros"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wholeEuros");
    }
}
