package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class CatalogTest {

    private final Catalog catalog = new Catalog(Map.of("LAMP-0031", 4_990L, "BULB-0007", 350L));

    private static boolean names(Throwable t, String method) {
        return Arrays.stream(t.getStackTrace()).anyMatch(f -> f.getMethodName().equals(method));
    }

    Throwable fromCart(String sku) {
        return catchThrowable(() -> catalog.price(sku));
    }

    Throwable fromSearch(String sku) {
        return catchThrowable(() -> catalog.price(sku));
    }

    @Test
    void pricesKnownSkusAndRejectsUnknownOnesByName() {
        assertThat(catalog.price("LAMP-0031")).isEqualTo(4_990L);
        assertThat(catalog.price("BULB-0007")).isEqualTo(350L);

        Throwable thrown = fromCart("LAMP-9999");

        assertThat(thrown).isInstanceOf(Catalog.UnknownSkuException.class).hasMessageContaining("LAMP-9999");
        assertThat(names(thrown, "fromCart")).isTrue();
    }

    @Test
    void searchFindsKnownSkusAndNothingForUnknownOnes() {
        assertThat(catalog.find("BULB-0007")).isEqualTo(OptionalLong.of(350L));
        assertThat(catalog.find("BULB-0008")).isEmpty();
    }

    @Test
    void searchingForUnknownSkusBuildsNoException() {
        AtomicInteger built = new AtomicInteger();
        Catalog counting = new Catalog(Map.of("DESK-0100", 21_900L)) {
            @Override
            UnknownSkuException unknown(String sku) {
                built.incrementAndGet();
                return super.unknown(sku);
            }
        };

        for (String typed : new String[]{"D", "DE", "DES", "DESK", "DESK-0100"}) {
            counting.find(typed);
        }

        assertThat(counting.find("DESK-0100")).isEqualTo(OptionalLong.of(21_900L));
        assertThat(built.get()).isZero();
    }

    @Test
    void aSecondCallerOfTheSameUnknownSkuSeesItsOwnTrace() {
        fromCart("BULB-0404");

        Throwable second = fromSearch("BULB-0404");

        assertThat(second).isInstanceOf(Catalog.UnknownSkuException.class);
        assertThat(names(second, "fromSearch")).isTrue();
    }

    @Test
    void anEarlierFailureKeepsItsTraceAfterALaterOne() {
        Throwable first = fromCart("SHADE-0012");

        fromSearch("SHADE-0012");

        assertThat(first).isInstanceOf(Catalog.UnknownSkuException.class);
        assertThat(names(first, "fromCart")).isTrue();
        assertThat(names(first, "fromSearch")).isFalse();
    }
}
