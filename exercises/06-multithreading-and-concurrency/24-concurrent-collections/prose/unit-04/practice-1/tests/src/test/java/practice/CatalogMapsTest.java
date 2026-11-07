package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogMapsTest {

    /** The length of the array behind a HashMap (or LinkedHashMap) or a ConcurrentHashMap; 0 before the first put. */
    private static int tableLength(Map<?, ?> map) {
        try {
            Class<?> owner = map instanceof ConcurrentHashMap<?, ?> ? ConcurrentHashMap.class : HashMap.class;
            Field table = owner.getDeclaredField("table");
            table.setAccessible(true);
            Object[] array = (Object[]) table.get(map);
            return array == null ? 0 : array.length;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("cannot read the table of " + map.getClass().getName(), e);
        }
    }

    private static String sku(int i) {
        return "SKU-" + (40_000 + i * 13);
    }

    /** Puts count entries and returns the table lengths seen after the first put and after the last. */
    private static int[] fill(Map<String, ? super Integer> map, int count) {
        map.put(sku(0), 0);
        int first = tableLength(map);
        for (int i = 1; i < count; i++) map.put(sku(i), i);
        return new int[] {first, tableLength(map)};
    }

    @Test
    void priceIndexHoldsEveryRowOfTheBatch() {
        Map<String, Long> index = CatalogMaps.newPriceIndex(1600);
        for (int i = 0; i < 1600; i++) index.put(sku(i), 100L * i);

        assertThat(index).hasSize(1600);
        assertThat(index.get(sku(1599))).isEqualTo(159_900L);
    }

    @Test
    void stockCacheHoldsEveryRowOfTheBatch() {
        ConcurrentHashMap<String, Integer> stock = CatalogMaps.newStockCache(1600);
        for (int i = 0; i < 1600; i++) stock.put(sku(i), i % 40);

        assertThat(stock).hasSize(1600);
        assertThat(stock.get(sku(41))).isEqualTo(1);
    }

    @Test
    void recentLookupsKeepAtMostTheirLimit() {
        CatalogMaps.RecentLookups recent = new CatalogMaps.RecentLookups(200);
        for (int i = 0; i < 500; i++) recent.put(sku(i), i);

        assertThat(recent.size()).isEqualTo(200);
        assertThat(recent.contains(sku(499))).isTrue();
        assertThat(recent.contains(sku(300))).isTrue();
        assertThat(recent.contains(sku(299))).isFalse();
    }

    @Test
    void priceIndexNeverGrowsItsTableWhileTheBatchLoads() {
        for (int skus : new int[] {200, 1600, 6500}) {
            Map<String, Long> index = CatalogMaps.newPriceIndex(skus);
            index.put(sku(0), 0L);
            int first = tableLength(index);
            for (int i = 1; i < skus; i++) index.put(sku(i), (long) i);

            assertThat(tableLength(index)).as("table after %d rows", skus).isEqualTo(first);
        }
    }

    @Test
    void stockCacheNeverGrowsItsTableWhileTheBatchLoads() {
        for (int skus : new int[] {200, 1600}) {
            int[] lengths = fill(CatalogMaps.newStockCache(skus), skus);

            assertThat(lengths[1]).as("table after %d rows", skus).isEqualTo(lengths[0]);
        }
    }

    @Test
    void stockCacheTableIsNoBiggerThanTheBatchNeeds() {
        for (int skus : new int[] {45, 700}) {
            int smallest = 1;
            while (smallest * 3 / 4 <= skus) smallest *= 2;   // the smallest table that holds them all

            int[] lengths = fill(CatalogMaps.newStockCache(skus), skus);

            assertThat(lengths[1]).as("table for %d rows", skus).isEqualTo(smallest);
        }
    }

    @Test
    void recentLookupsEvictTheEntryUsedLeastRecently() {
        CatalogMaps.RecentLookups recent = new CatalogMaps.RecentLookups(150);
        for (int i = 0; i < 150; i++) recent.put(sku(i), i);

        assertThat(recent.get(sku(0))).isEqualTo(0L);       // the oldest entry is read again
        recent.put(sku(150), 150);

        assertThat(recent.contains(sku(0))).as("the entry just read").isTrue();
        assertThat(recent.contains(sku(1))).as("the entry nobody read").isFalse();
    }

    @Test
    void recentLookupsNeverGrowTheirTable() {
        for (int limit : new int[] {200, 1600}) {
            CatalogMaps.RecentLookups recent = new CatalogMaps.RecentLookups(limit);
            recent.put(sku(0), 0);
            int first = tableLength(recent.table());
            for (int i = 1; i < 3 * limit; i++) recent.put(sku(i), i);

            assertThat(tableLength(recent.table())).as("table with a limit of %d", limit).isEqualTo(first);
        }
    }
}
