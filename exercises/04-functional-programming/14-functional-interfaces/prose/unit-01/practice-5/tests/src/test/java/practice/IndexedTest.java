package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndexedTest {

    @Test
    void passesEachElementWithItsIndex() {
        List<String> seen = new ArrayList<>();
        Indexed.forEachIndexed(List.of("Alice", "Bob", "Charlie"), (s, i) -> seen.add(i + ": " + s));
        assertThat(seen).containsExactly("0: Alice", "1: Bob", "2: Charlie");

        List<String> none = new ArrayList<>();
        Indexed.forEachIndexed(List.<String>of(), (s, i) -> none.add(s));
        assertThat(none).isEmpty();
    }

    @Test
    void countsValuesIntoBuckets() {
        assertThat(Indexed.histogram(new int[] {1, 1, 2, 0}, 3)).containsExactly(1, 2, 1);
        assertThat(Indexed.histogram(new int[] {}, 2)).containsExactly(0, 0);
    }

    @Test
    void duplicatesGetTheirOwnIndex() {
        List<String> seen = new ArrayList<>();
        Indexed.forEachIndexed(List.of("a", "b", "a", "a"), (s, i) -> seen.add(i + ": " + s));
        assertThat(seen).containsExactly("0: a", "1: b", "2: a", "3: a");
    }

    @Test
    void outOfRangeValuesAreIgnored() {
        assertThat(Indexed.histogram(new int[] {-1, 0, 3, 2, 7}, 3)).containsExactly(1, 0, 1);
    }

    @Test
    void aLargeHistogramLosesNoCounts() throws Exception {
        int[] values = new int[4_000_000];
        for (int i = 0; i < values.length; i++) {
            values[i] = i % 4;
        }
        java.util.concurrent.ForkJoinPool pool = new java.util.concurrent.ForkJoinPool(4);
        try {
            int[] counts = pool.submit(() -> Indexed.histogram(values, 4)).get();
            assertThat(counts).containsExactly(1_000_000, 1_000_000, 1_000_000, 1_000_000);
        } finally {
            pool.shutdown();
        }
    }
}
