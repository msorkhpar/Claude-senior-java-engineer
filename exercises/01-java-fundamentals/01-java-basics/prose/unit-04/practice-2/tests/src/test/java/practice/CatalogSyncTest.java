package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogSyncTest {

    private static CatalogSync.PriceChange change(String sku, long cents) {
        return new CatalogSync.PriceChange(sku, cents);
    }

    private static CatalogSync catalog() {
        return new CatalogSync(Map.of("TEA-9", 1_250L, "MUG-3", 899L, "KETTLE-1", 4_590L));
    }

    @Test
    void appliesAChangeAndReportsWhetherTheCatalogChanged() {
        CatalogSync sync = catalog();
        assertThat(sync.sync(List.of(change("MUG-3", 949)))).isTrue();
        assertThat(sync.price("MUG-3")).isEqualTo(949L);

        assertThat(sync.sync(List.of(change("TEA-9", 1_250)))).isFalse();
        assertThat(sync.sync(List.of())).isFalse();
        assertThat(sync.price("TEA-9")).isEqualTo(1_250L);
    }

    @Test
    void everyChangeInTheFeedReachesTheCatalog() {
        CatalogSync sync = catalog();
        boolean changed = sync.sync(List.of(change("KETTLE-1", 4_200), change("TEA-9", 1_300),
                change("SPOON-12", 199)));

        assertThat(changed).isTrue();
        assertThat(sync.price("KETTLE-1")).isEqualTo(4_200L);
        assertThat(sync.price("TEA-9")).isEqualTo(1_300L);
        assertThat(sync.price("SPOON-12")).isEqualTo(199L);
    }

    @Test
    void anEarlierChangeIsStillReportedWhenTheLastChangesNothing() {
        CatalogSync sync = catalog();
        boolean changed = sync.sync(List.of(change("MUG-3", 1_099), change("TEA-9", 1_250)));

        assertThat(changed).isTrue();
        assertThat(sync.price("MUG-3")).isEqualTo(1_099L);
    }

    @Test
    void aRejectedChangeLeavesThePriceAlone() {
        CatalogSync sync = catalog();
        boolean changed = sync.sync(List.of(change("KETTLE-1", -4_590), change("TEA-9", 0)));

        assertThat(changed).isFalse();
        assertThat(sync.price("KETTLE-1")).isEqualTo(4_590L);
        assertThat(sync.price("TEA-9")).isEqualTo(1_250L);
    }

    @Test
    void aChangeWithoutASkuIsSkippedWithoutBreakingTheSync() {
        CatalogSync sync = catalog();
        List<CatalogSync.PriceChange> feed = new ArrayList<>(Arrays.asList(
                change(null, 500), change("  ", 700), change("MUG-3", 1_049)));

        boolean changed = sync.sync(feed);

        assertThat(changed).isTrue();
        assertThat(sync.price("MUG-3")).isEqualTo(1_049L);
        assertThat(sync.price(null)).isNull();
    }
}
