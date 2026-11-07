package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import practice.RateCache.Shipment;

class RateCacheTest {

    /** A fresh map each call, with new String keys and weights outside the Integer cache. */
    private static Map<String, Integer> parcels(Object... idAndGrams) {
        Map<String, Integer> m = new HashMap<>();
        for (int i = 0; i < idAndGrams.length; i += 2) {
            m.put(new String((String) idAndGrams[i]), (Integer) idAndGrams[i + 1]);
        }
        return m;
    }

    @Test
    void shipmentsBuiltFromEqualParcelsShareARate() {
        RateCache cache = new RateCache();
        cache.put(new Shipment(new String("UPX"), parcels("p-31", 2400, "p-32", 750)), 1825L);

        Long rate = cache.rateFor(new Shipment(new String("UPX"), parcels("p-32", 750, "p-31", 2400)));

        assertThat(rate).isEqualTo(1825L);
        assertThat(cache.rateFor(new Shipment("UPX", parcels("p-31", 2400, "p-32", 751)))).isNull();
    }

    @Test
    void withParcelAddsAParcelAndLeavesTheOriginalQuoted() {
        RateCache cache = new RateCache();
        Shipment two = new Shipment("DHQ", parcels("a-1", 1300, "a-2", 980));
        cache.put(two, 2210L);

        Shipment three = two.withParcel("a-3", 4100);

        assertThat(three.carrier()).isEqualTo("DHQ");
        assertThat(three.parcels()).containsOnly(
                Map.entry("a-1", 1300), Map.entry("a-2", 980), Map.entry("a-3", 4100));
        assertThat(two.parcels()).hasSize(2);
        assertThat(cache.rateFor(two)).isEqualTo(2210L);
        assertThat(cache.rateFor(three)).isNull();
    }

    @Test
    void aCallerThatReusesItsMapDoesNotMoveACachedShipment() {
        RateCache cache = new RateCache();
        Map<String, Integer> buffer = parcels("k-7", 5200);
        Shipment shipment = new Shipment("FDZ", buffer);
        cache.put(shipment, 3990L);

        buffer.put("k-8", 610);                       // the caller reuses its map for the next request

        assertThat(shipment.parcels()).containsOnly(Map.entry("k-7", 5200));
        assertThat(cache.rateFor(shipment)).isEqualTo(3990L);
        assertThat(cache.rateFor(new Shipment("FDZ", parcels("k-7", 5200)))).isEqualTo(3990L);
    }

    @Test
    void codeHoldingAShipmentCannotChangeItsParcels() {
        RateCache cache = new RateCache();
        Shipment shipment = new Shipment("FDZ", parcels("m-1", 330, "m-2", 1440));
        cache.put(shipment, 1250L);

        try {
            shipment.parcels().remove("m-2");          // somebody "trims" a shipment they were handed
        } catch (UnsupportedOperationException expected) {
            // refusing is one way to keep it
        }

        assertThat(shipment.parcels()).hasSize(2);
        assertThat(cache.rateFor(shipment)).isEqualTo(1250L);
    }
}
