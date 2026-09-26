package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class VisitsTest {

    /**
     * A map whose reads (get, getOrDefault, containsKey, putIfAbsent's answer and the entrySet, keySet and
     * values views) line two worker threads up: each reads, then waits (at most 3 s) until the other has also
     * read, so a solution that reads and then writes in a separate call always has both threads read the old
     * value before either writes. The map's own single-step updates (merge, compute) never call these.
     */
    private static final class SteppedMap extends ConcurrentHashMap<String, Integer> {
        private final CyclicBarrier bothRead = new CyclicBarrier(2);
        private final Set<Thread> workers = ConcurrentHashMap.newKeySet();

        private void step() {
            if (workers.remove(Thread.currentThread())) {
                try {
                    bothRead.await(3, TimeUnit.SECONDS);
                } catch (TimeoutException | BrokenBarrierException e) {
                    // the other thread never read: go on alone
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public Integer get(Object key) {
            Integer seen = super.get(key);
            step();
            return seen;
        }

        @Override
        public Integer getOrDefault(Object key, Integer fallback) {
            Integer seen = super.getOrDefault(key, fallback);
            step();
            return seen;
        }

        @Override
        public boolean containsKey(Object key) {
            boolean seen = super.containsKey(key);
            step();
            return seen;
        }

        @Override
        public Integer putIfAbsent(String key, Integer value) {
            Integer seen = super.putIfAbsent(key, value);
            step();
            return seen;
        }

        @Override
        public Set<Map.Entry<String, Integer>> entrySet() {
            Set<Map.Entry<String, Integer>> seen = super.entrySet();
            step();
            return seen;
        }

        @Override
        public KeySetView<String, Integer> keySet() {
            KeySetView<String, Integer> seen = super.keySet();
            step();
            return seen;
        }

        @Override
        public Collection<Integer> values() {
            Collection<Integer> seen = super.values();
            step();
            return seen;
        }
    }

    private static String page(String name) {
        return new String(name.toCharArray());
    }

    @Test
    void countsVisits() {
        ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
        counts.put(page("home"), 1000);
        counts.put(page("about"), 500);
        Visits.record(counts, page("home"));
        Visits.record(counts, page("home"));
        Visits.record(counts, page("about"));
        assertThat(counts).containsEntry("home", 1002).containsEntry("about", 501).hasSize(2);
    }

    @Test
    void firstVisitCountsOne() {
        ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
        Visits.record(counts, page("about"));
        assertThat(counts).containsEntry("about", 1);
        Visits.record(counts, page("about"));
        assertThat(counts).containsEntry("about", 2);
    }

    @Test
    void simultaneousVisitsAreNotLost() throws InterruptedException {
        SteppedMap counts = new SteppedMap();
        counts.put(page("home"), 1000);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread[] threads = new Thread[2];
        for (int i = 0; i < 2; i++) {
            threads[i] = new Thread(() -> Visits.record(counts, page("home")));
            threads[i].setDaemon(true);
            threads[i].setUncaughtExceptionHandler((t, e) -> failure.set(e));
            counts.workers.add(threads[i]);
        }
        threads[0].start();
        threads[1].start();
        threads[0].join(8_000);
        threads[1].join(8_000);
        assertThat(threads[0].isAlive() || threads[1].isAlive()).as("both visits finished").isFalse();
        assertThat(failure.get()).isNull();
        assertThat(counts.get("home")).isEqualTo(1002);
    }
}
