package practice;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class CountingStoreTest {

    private static Map<String, Long> unsorted() {
        Map<String, Long> batch = new LinkedHashMap<>();
        batch.put("SKU-7731", 1_299L);
        batch.put("SKU-0042", 450L);
        batch.put("SKU-5108", 8_990L);
        batch.put("SKU-2265", 75L);
        return batch;
    }

    @Test
    void singleWritesAreStoredAndCountedOverwritesIncluded() {
        CountingStore store = new CountingStore();

        store.put("SKU-0042", 450L);
        store.put("SKU-7731", 1_299L);
        Long previous = store.put("SKU-0042", 425L);

        assertThat(previous).isEqualTo(450L);
        assertThat(store.get("SKU-0042")).isEqualTo(425L);
        assertThat(store.firstKey()).isEqualTo("SKU-0042");
        assertThat(store.writes()).isEqualTo(3);
    }

    @Test
    void everyEntryOfABatchIsStoredInOrder() {
        CountingStore fromHash = new CountingStore();
        fromHash.putAll(new HashMap<>(unsorted()));
        CountingStore fromSorted = new CountingStore();
        fromSorted.putAll(new TreeMap<>(unsorted()));

        assertThat(fromHash).containsExactlyEntriesOf(new TreeMap<>(unsorted()));
        assertThat(fromSorted).containsExactlyEntriesOf(new TreeMap<>(unsorted()));
    }

    @Test
    void aSortedSnapshotLoadedIntoAnEmptyStoreIsCounted() {
        CountingStore store = new CountingStore();

        store.putAll(new TreeMap<>(unsorted()));

        assertThat(store.writes()).isEqualTo(4);
    }

    @Test
    void aBatchFromAnUnsortedMapIsCountedOncePerEntry() {
        CountingStore store = new CountingStore();
        store.put("SKU-9001", 19_900L);

        store.putAll(unsorted());

        assertThat(store.writes()).isEqualTo(5);
    }
}
