package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;

class LeaseTableTest {

    /**
     * The table's store. Just before the sweeper's next change to a chosen resource, it runs what
     * the holder does on its own thread at that moment: the forced interleaving.
     */
    private static final class InterleavingStore extends ConcurrentHashMap<String, LeaseTable.Lease> {
        private final Map<Object, Runnable> justBefore = new HashMap<>();

        void justBeforeAChangeTo(String resource, Runnable holderAction) {
            justBefore.put(resource, holderAction);
        }

        private void interleave(Object key) {
            Runnable action = justBefore.remove(key);
            if (action != null) action.run();
        }

        @Override
        public LeaseTable.Lease remove(Object key) {
            interleave(key);
            return super.remove(key);
        }

        @Override
        public boolean remove(Object key, Object value) {
            interleave(key);
            return super.remove(key, value);
        }

        @Override
        public LeaseTable.Lease computeIfPresent(String key,
                BiFunction<? super String, ? super LeaseTable.Lease, ? extends LeaseTable.Lease> f) {
            interleave(key);
            return super.computeIfPresent(key, f);
        }

        @Override
        public LeaseTable.Lease compute(String key,
                BiFunction<? super String, ? super LeaseTable.Lease, ? extends LeaseTable.Lease> f) {
            interleave(key);
            return super.compute(key, f);
        }
    }

    private final InterleavingStore store = new InterleavingStore();
    private final LeaseTable table = new LeaseTable(store);
    private final List<LeaseTable.Lease> notified = new ArrayList<>();

    private void threeLeases() {
        table.acquire("agent-linux-2", "pipeline-410", 1_000);
        table.acquire("gpu-7", "training-job-12", 5_000);
        table.acquire("plotter", "cad-team", 2_000);
    }

    @Test
    void freesTheLeasesThatRanOutAndKeepsTheOthers() {
        threeLeases();

        List<String> freed = table.sweep(2_000, notified::add);

        assertThat(freed).containsExactlyInAnyOrder("agent-linux-2", "plotter");
        assertThat(notified).extracting(LeaseTable.Lease::holder)
                .containsExactlyInAnyOrder("pipeline-410", "cad-team");
        assertThat(table.holderOf("agent-linux-2")).isNull();
        assertThat(table.holderOf("plotter")).isNull();
        assertThat(table.holderOf("gpu-7").holder()).isEqualTo("training-job-12");
    }

    @Test
    void aSweepBeforeAnythingRanOutFreesNothing() {
        threeLeases();

        assertThat(table.sweep(999, notified::add)).isEmpty();
        assertThat(notified).isEmpty();
        assertThat(table.holderOf("agent-linux-2").holder()).isEqualTo("pipeline-410");
    }

    @Test
    void aLeaseRenewedJustAsTheSweeperFreesItStaysWithItsHolder() {
        threeLeases();
        store.justBeforeAChangeTo("plotter", () -> table.renew("plotter", "cad-team", 9_000));

        table.sweep(2_000, notified::add);

        assertThat(table.holderOf("plotter")).isEqualTo(new LeaseTable.Lease("plotter", "cad-team", 9_000));
        assertThat(table.holderOf("agent-linux-2")).isNull();
    }

    @Test
    void aLeaseReleasedJustAsTheSweeperFreesItIsNotReportedAsExpired() {
        threeLeases();
        store.justBeforeAChangeTo("plotter", () -> table.release("plotter", "cad-team"));

        List<String> freed = table.sweep(2_000, notified::add);

        assertThat(freed).containsExactly("agent-linux-2");
        assertThat(notified).extracting(LeaseTable.Lease::holder).containsExactly("pipeline-410");
        assertThat(table.holderOf("plotter")).isNull();
    }
}
