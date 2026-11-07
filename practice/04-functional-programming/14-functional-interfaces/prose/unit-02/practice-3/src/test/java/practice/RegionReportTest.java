package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RegionReportTest {

    private static Thread daemon(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }

    @Test
    void oneRegionsSkusComeOutSortedWithoutRepeats() {
        RegionReport report = new RegionReport();
        Map<String, SortedSet<String>> byRegion = report.skusByRegion(List.of(
                new RegionReport.Sale("alpine", "tent-2p"), new RegionReport.Sale("alpine", "axe"),
                new RegionReport.Sale("alpine", "tent-2p"), new RegionReport.Sale("alpine", "rope-30m")));

        assertThat(byRegion).containsOnlyKeys("alpine");
        assertThat(byRegion.get("alpine")).containsExactly("axe", "rope-30m", "tent-2p");
    }

    @Test
    void oneThreadRendersReportAfterReportIntoTheSameBuffer() {
        RegionReport report = new RegionReport();
        Map<String, SortedSet<String>> first = new TreeMap<>(Map.of("coastal", new TreeSet<>(List.of("kayak", "paddle"))));
        Map<String, SortedSet<String>> second = new TreeMap<>(Map.of("delta", new TreeSet<>(List.of("waders"))));

        assertThat(report.render(first)).isEqualTo("coastal: kayak, paddle\n");
        StringBuilder used = report.scratch.get();
        assertThat(report.render(second)).isEqualTo("delta: waders\n");
        assertThat(report.scratch.get()).isSameAs(used);
    }

    @Test
    void eachRegionListsOnlyWhatSoldThere() {
        RegionReport report = new RegionReport();
        Map<String, SortedSet<String>> byRegion = report.skusByRegion(List.of(
                new RegionReport.Sale("alpine", "axe"), new RegionReport.Sale("coastal", "kayak"),
                new RegionReport.Sale("alpine", "rope-30m"), new RegionReport.Sale("delta", "waders")));

        assertThat(byRegion.get("alpine")).containsExactly("axe", "rope-30m");
        assertThat(byRegion.get("coastal")).containsExactly("kayak");
        assertThat(byRegion.get("delta")).containsExactly("waders");
    }

    @Test
    void twoRenderingThreadsNeverShareABuffer() throws InterruptedException {
        RegionReport report = new RegionReport();
        AtomicReference<StringBuilder> seenByA = new AtomicReference<>();
        AtomicReference<StringBuilder> seenByB = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(2);
        daemon(() -> { seenByA.set(report.scratch.get()); done.countDown(); }).start();
        daemon(() -> { seenByB.set(report.scratch.get()); done.countDown(); }).start();

        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(seenByA.get()).isNotNull();
        assertThat(seenByB.get()).isNotNull().isNotSameAs(seenByA.get());
        assertThat(report.scratch.get()).as("the test thread's buffer")
                .isNotSameAs(seenByA.get()).isNotSameAs(seenByB.get());
    }
}
