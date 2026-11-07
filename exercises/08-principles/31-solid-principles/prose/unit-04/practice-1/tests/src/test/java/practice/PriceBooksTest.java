package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PriceBooksTest {

    /** A source that can only be read: no PriceEditor anywhere. */
    private static PriceBooks.PriceLookup readOnlySource(Map<String, Long> prices) {
        return new PriceBooks.PriceLookup() {
            @Override
            public long priceOf(String sku) {
                return prices.get(sku);
            }

            @Override
            public Set<String> skus() {
                return prices.keySet();
            }
        };
    }

    private static PriceBooks.LivePriceBook liveBook() {
        PriceBooks.LivePriceBook live = new PriceBooks.LivePriceBook();
        live.setPrice("kettle-k2", 4_990);
        live.setPrice("mug-blue", 1_200);
        live.setPrice("teapot-t9", 7_500);
        return live;
    }

    @Test
    void aFrozenBookAnswersWhatItsSourceAnsweredWhenFrozen() {
        PriceBooks.FrozenPriceBook frozen = PriceBooks.FrozenPriceBook.of(
                readOnlySource(Map.of("lamp-l4", 3_333L, "rug-r1", 15_000L)));

        assertThat(frozen.skus()).containsExactlyInAnyOrder("lamp-l4", "rug-r1");
        assertThat(frozen.priceOf("lamp-l4")).isEqualTo(3_333L);
        assertThat(frozen.priceOf("rug-r1")).isEqualTo(15_000L);
    }

    @Test
    void repricingTheLiveBookAfterwardsLeavesTheFrozenBookAlone() {
        PriceBooks.LivePriceBook live = liveBook();
        PriceBooks.FrozenPriceBook frozen = PriceBooks.FrozenPriceBook.of(live);

        new PriceBooks.Repricer(live).discount(20);

        assertThat(live.priceOf("teapot-t9")).isEqualTo(6_000L);
        assertThat(frozen.priceOf("teapot-t9")).isEqualTo(7_500L);
        assertThat(frozen.priceOf("mug-blue")).isEqualTo(1_200L);
    }

    @Test
    void nothingThatChangesPricesCanBeHandedAFrozenBook() {
        assertThat(PriceBooks.PriceEditor.class.isAssignableFrom(PriceBooks.FrozenPriceBook.class))
                .as("a frozen book is a PriceEditor, so the compiler lets the repricer take it")
                .isFalse();
        for (Constructor<?> c : PriceBooks.Repricer.class.getConstructors()) {
            for (Class<?> parameter : c.getParameterTypes()) {
                assertThat(parameter.isAssignableFrom(PriceBooks.FrozenPriceBook.class))
                        .as("Repricer's constructor accepts a frozen book")
                        .isFalse();
            }
        }
    }

    @Test
    void checkoutReplaysABasketAgainstAFrozenBook() throws Exception {
        PriceBooks.FrozenPriceBook march = PriceBooks.FrozenPriceBook.of(liveBook());

        // Built by reflection, so this test compiles whatever type the constructor takes.
        Constructor<?> constructor = PriceBooks.Checkout.class.getConstructors()[0];
        assertThat(constructor.getParameterTypes()[0].isInstance(march))
                .as("Checkout's constructor takes a %s, which a frozen book is not",
                        constructor.getParameterTypes()[0].getSimpleName())
                .isTrue();
        PriceBooks.Checkout checkout = (PriceBooks.Checkout) constructor.newInstance(march);

        assertThat(checkout.total(Map.of("kettle-k2", 2, "mug-blue", 3))).isEqualTo(13_580L);
    }
}
