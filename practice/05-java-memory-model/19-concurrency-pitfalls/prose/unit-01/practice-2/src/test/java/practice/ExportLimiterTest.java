package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ExportLimiterTest {

    @Test
    void startsUpToTheLimitPerCustomer() throws Exception {
        ExportLimiter limiter = new ExportLimiter(2, new ConcurrentHashMap<>());
        assertThat(call(() -> limiter.tryStart("orbit"))).isTrue();
        assertThat(call(() -> limiter.tryStart("orbit"))).isTrue();
        assertThat(call(() -> limiter.tryStart("orbit"))).as("a third export of orbit, limit 2").isFalse();
        assertThat(call(() -> limiter.tryStart("vela"))).as("vela's first export").isTrue();
        assertThat(limiter.running("vela")).isEqualTo(1);
    }

    @Test
    void anExportThatEndsFreesItsSlot() throws Exception {
        ExportLimiter limiter = new ExportLimiter(1, new ConcurrentHashMap<>());
        assertThat(call(() -> limiter.tryStart("lumen"))).isTrue();
        call(() -> { limiter.finish("lumen"); return null; });
        assertThat(limiter.running("lumen")).isZero();
        assertThat(call(() -> limiter.tryStart("lumen"))).isTrue();
        call(() -> { limiter.finish("lumen"); return null; });
        assertThat(call(() -> limiter.tryStart("lumen"))).isTrue();
        assertThat(limiter.running("lumen")).isEqualTo(1);
    }

    @Test
    void twoStartsForTheLastSlotLetOneThrough() throws Exception {
        PausingMap counts = new PausingMap();
        ExportLimiter limiter = new ExportLimiter(3, counts);
        assertThat(call(() -> limiter.tryStart("kestrel"))).isTrue();
        assertThat(call(() -> limiter.tryStart("kestrel"))).isTrue();

        FutureTask<Boolean> first = counts.pausedAfterItsFirstRead(() -> limiter.tryStart("kestrel"));
        FutureTask<Boolean> second = counts.runWhileThePausedOneWaits(() -> limiter.tryStart("kestrel"));

        boolean firstStarted = first.get(10, TimeUnit.SECONDS);
        boolean secondStarted = second.get(10, TimeUnit.SECONDS);
        assertThat(firstStarted ^ secondStarted)
                .as("exactly one of the two starts got the last slot (first: " + firstStarted + ", second: " + secondStarted + ")")
                .isTrue();
        assertThat(limiter.running("kestrel")).as("exports of kestrel counted as running").isEqualTo(3);
    }

    @Test
    void twoExportsEndingTogetherFreeTwoSlots() throws Exception {
        PausingMap counts = new PausingMap();
        ExportLimiter limiter = new ExportLimiter(3, counts);
        for (int i = 0; i < 3; i++) assertThat(call(() -> limiter.tryStart("heron"))).isTrue();

        FutureTask<Object> first = counts.pausedAfterItsFirstRead(() -> { limiter.finish("heron"); return null; });
        FutureTask<Object> second = counts.runWhileThePausedOneWaits(() -> { limiter.finish("heron"); return null; });
        first.get(10, TimeUnit.SECONDS);
        second.get(10, TimeUnit.SECONDS);

        assertThat(limiter.running("heron")).as("exports of heron still running after two of three ended").isEqualTo(1);
        assertThat(call(() -> limiter.tryStart("heron"))).isTrue();
        assertThat(call(() -> limiter.tryStart("heron"))).as("the second freed slot").isTrue();
    }

    @Test
    void aRefusedStartTakesNoSlot() throws Exception {
        ExportLimiter limiter = new ExportLimiter(1, new ConcurrentHashMap<>());
        assertThat(call(() -> limiter.tryStart("quill"))).isTrue();
        assertThat(call(() -> limiter.tryStart("quill"))).isFalse();
        call(() -> { limiter.finish("quill"); return null; });
        assertThat(call(() -> limiter.tryStart("quill"))).as("a start after the only export ended").isTrue();
    }

    /** Runs the call on its own daemon thread and waits for it, so a call that never returns fails the test. */
    private static <T> T call(Callable<T> body) throws Exception {
        return start(body).get(10, TimeUnit.SECONDS);
    }

    private static <T> FutureTask<T> start(Callable<T> body) {
        FutureTask<T> task = new FutureTask<>(body);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        return task;
    }

    /**
     * A count map that can pause one chosen thread right after its first read (get, containsKey or
     * getOrDefault), until a second call has had its turn. Writes are never paused.
     */
    static final class PausingMap extends ConcurrentHashMap<String, Integer> {
        private final AtomicReference<Thread> pauseWho = new AtomicReference<>();
        private final CountDownLatch pausedOrDone = new CountDownLatch(1);
        private final CountDownLatch goOn = new CountDownLatch(1);

        /** Starts the call on a thread that will be paused after its first read of this map. */
        <T> FutureTask<T> pausedAfterItsFirstRead(Callable<T> body) throws InterruptedException {
            FutureTask<T> task = new FutureTask<>(() -> {
                try {
                    return body.call();
                } finally {
                    pausedOrDone.countDown();
                }
            });
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            pauseWho.set(thread);
            thread.start();
            assertThat(pausedOrDone.await(10, TimeUnit.SECONDS)).as("the first call read the map or finished").isTrue();
            return task;
        }

        /**
         * Runs a second call while the first is paused, then lets the first go on. A second call
         * that waits for the first one (for a lock it holds) is let through after a bounded wait.
         */
        <T> FutureTask<T> runWhileThePausedOneWaits(Callable<T> body) throws Exception {
            FutureTask<T> task = start(body);
            try {
                task.get(2, TimeUnit.SECONDS);
            } catch (TimeoutException waitsForTheFirst) {
                // it is waiting for the paused call; that is allowed
            } finally {
                pauseWho.set(null);
                goOn.countDown();
            }
            return task;
        }

        private void pauseIfChosen() {
            if (pauseWho.compareAndSet(Thread.currentThread(), null)) {
                pausedOrDone.countDown();
                try {
                    goOn.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public Integer get(Object key) {
            Integer value = super.get(key);
            pauseIfChosen();
            return value;
        }

        @Override
        public boolean containsKey(Object key) {
            boolean present = super.containsKey(key);
            pauseIfChosen();
            return present;
        }

        @Override
        public Integer getOrDefault(Object key, Integer defaultValue) {
            Integer value = super.getOrDefault(key, defaultValue);
            pauseIfChosen();
            return value;
        }
    }
}
