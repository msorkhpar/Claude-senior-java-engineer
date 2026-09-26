package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class WordCounterTest {

    @Test
    void countsWords() {
        WordCounter counter = new WordCounter(new ConcurrentHashMap<>());
        counter.add("to");
        counter.add("be");
        counter.add("to");
        assertThat(counter.count("to")).isEqualTo(2);
        assertThat(counter.count("be")).isEqualTo(1);
        assertThat(counter.count("or")).isZero();

        // Counts above 127 (outside the Integer cache), under keys built at run time.
        for (int i = 0; i < 300; i++) {
            counter.add(new StringBuilder("wor").append('d').toString());
        }
        assertThat(counter.count(new String("word"))).isEqualTo(300);
    }

    @Test
    void racingAddsOfOneWordBothCount() throws InterruptedException {
        GatedMap counts = new GatedMap();
        WordCounter counter = new WordCounter(counts);
        Thread first = daemon(() -> counter.add("x"));
        // If the first add reads the count on its own, that read is held open after it has read.
        waitFor(() -> counts.readHeld.getCount() == 0 || !first.isAlive(), "the first add to read or finish");
        Thread second = daemon(() -> counter.add("x"));
        waitFor(() -> !second.isAlive() || blockedBy(second, first), "the second add to finish or wait");
        counts.release.countDown();
        first.join(5_000);
        second.join(5_000);
        assertThat(first.isAlive() || second.isAlive()).as("both adds finished").isFalse();
        assertThat(counter.count("x")).as("count after two adds").isEqualTo(2);
    }

    /** A map whose first plain read returns what it read only after the test lets it go. */
    static final class GatedMap extends ConcurrentHashMap<String, Integer> {
        final CountDownLatch readHeld = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        private boolean used;

        @Override
        public Integer get(Object key) {
            Integer value = super.get(key);
            boolean hold;
            synchronized (this.release) {
                hold = !used && release.getCount() > 0;
                used = true;
            }
            if (hold) {
                readHeld.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return value;
        }

        @Override
        public Integer getOrDefault(Object key, Integer fallback) {
            Integer value = get(key);
            return value == null ? fallback : value;
        }

        @Override
        public boolean containsKey(Object key) {
            return get(key) != null;
        }
    }

    /** True only when the thread waits for a lock that the given holder thread owns. */
    private static boolean blockedBy(Thread waiter, Thread holder) {
        java.lang.management.ThreadInfo info =
                java.lang.management.ManagementFactory.getThreadMXBean().getThreadInfo(waiter.threadId());
        return info != null && info.getLockOwnerId() == holder.threadId();
    }

    private static Thread daemon(Runnable body) {
        Thread thread = new Thread(body);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static void waitFor(BooleanSupplier condition, String what) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            assertThat(System.nanoTime() < deadline).as("timed out waiting for " + what).isTrue();
            Thread.sleep(1);
        }
    }
}
