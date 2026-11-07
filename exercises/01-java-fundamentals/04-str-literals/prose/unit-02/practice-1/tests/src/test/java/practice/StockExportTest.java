package practice;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import practice.StockExport.Item;

import static org.assertj.core.api.Assertions.assertThat;

class StockExportTest {

    /** What one call to csv allocated on the thread that ran it, and what it returned. */
    private record Run(String csv, long allocatedBytes) { }

    /** Runs csv on a daemon thread of its own, so a slow solution can't hang the build. */
    private static Run measured(List<Item> items) throws Exception {
        com.sun.management.ThreadMXBean mx =
                (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        assertThat(mx.isThreadAllocatedMemorySupported()).isTrue();
        mx.setThreadAllocatedMemoryEnabled(true);
        ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            Future<Run> run = pool.submit(() -> {
                long id = Thread.currentThread().threadId();
                long before = mx.getThreadAllocatedBytes(id);
                String csv = StockExport.csv(items);
                return new Run(csv, mx.getThreadAllocatedBytes(id) - before);
            });
            return run.get(120, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }

    private static List<Item> catalogue(int size) {
        List<Item> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add(new Item("WH-" + (100_000 + i), i % 37));
        }
        return items;
    }

    @Test
    void writesTheHeaderAndOneLinePerItemInOrder() {
        String csv = StockExport.csv(List.of(new Item("TX-9", 12), new Item("AB-301", 0),
                new Item("TX-10", 5)));

        assertThat(csv.lines().toList())
                .containsExactly("sku,qty", "TX-9,12", "AB-301,0", "TX-10,5");
    }

    @Test
    void largeCatalogueIsBuiltWithoutCopyingEarlierLines() throws Exception {
        List<Item> items = catalogue(40_000);

        Run run = measured(items);

        assertThat(run.csv().lines().count()).isEqualTo(40_001);
        assertThat(run.csv()).endsWith("\nWH-139999,2");
        // The file is about 0.5 MB. Building it in one buffer allocates a few MB; copying the
        // text so far for every line allocates several GB.
        assertThat(run.allocatedBytes()).isLessThan(100_000_000L);
    }

    @Test
    void lastLineHasNoNewlineAfterIt() {
        assertThat(StockExport.csv(List.of(new Item("QQ-5", 3))))
                .isEqualTo("sku,qty\nQQ-5,3");
        assertThat(StockExport.csv(List.of(new Item("M-1", 40), new Item("M-2", 41))))
                .isEqualTo("sku,qty\nM-1,40\nM-2,41");
    }

    @Test
    void emptyCatalogueIsTheHeaderAlone() {
        assertThat(StockExport.csv(List.of())).isEqualTo("sku,qty");
    }
}
