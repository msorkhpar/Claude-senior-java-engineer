package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.fail;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class StocksTest {

    /** A stock whose set() stops inside, until the test lets it go. */
    static final class Gate implements Stocks.Stock {
        final CountDownLatch inside = new CountDownLatch(1);
        final CountDownLatch letGo = new CountDownLatch(1);

        @Override
        public int level(String sku) {
            return 11;
        }

        @Override
        public void set(String sku, int units) {
            inside.countDown();
            try {
                letGo.await(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static Thread daemon(String name, Runnable r) {
        Thread t = new Thread(r, name);
        t.setDaemon(true);
        t.start();
        return t;
    }

    /** Waits until 'waiter' is stopped on a lock 'owner' holds; fails if it finishes instead. */
    private static void awaitBlockedBehind(Thread waiter, Thread owner) throws InterruptedException {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < end) {
            if (!waiter.isAlive()) fail("the second call finished while the first was still inside the stock");
            ThreadInfo info = mx.getThreadInfo(waiter.threadId());
            if (info != null && info.getLockOwnerId() == owner.threadId()) return;
            Thread.onSpinWait();
        }
        fail("the second call neither waited for the first nor finished within 10 s");
    }

    @Test
    void theSharedStockAnswersAndCountsLikeTheStoreItWraps() {
        Stocks.Shared shared = Stocks.shared(new Stocks.MapStock());
        shared.stock().set("wool-scarf", 6);
        shared.stock().set("clay-bowl", 0);
        assertThat(shared.stock().level("wool-scarf")).isEqualTo(6);
        assertThat(shared.stock().level("clay-bowl")).isZero();
        assertThat(catchThrowable(() -> shared.stock().set("clay-bowl", -4)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(shared.metrics().succeeded()).isEqualTo(4);
        assertThat(shared.metrics().failed()).isEqualTo(1);
        assertThat(shared.metrics().inFlight()).isZero();
    }

    @Test
    void manyThreadsCanSetLevelsThroughTheSharedStock() throws Exception {
        Stocks.Shared shared = Stocks.shared(new Stocks.MapStock());
        ExecutorService pool = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            List<Future<?>> done = new ArrayList<>();
            for (int t = 0; t < 4; t++) {
                int th = t;
                done.add(pool.submit(() -> {
                    for (int i = 0; i < 5_000; i++) shared.stock().set("bin-" + th + "-" + i, i % 50);
                }));
            }
            for (Future<?> f : done) f.get(30, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
        for (int t = 0; t < 4; t++) {
            for (int i = 0; i < 5_000; i++) {
                assertThat(shared.stock().level("bin-" + t + "-" + i)).isEqualTo(i % 50);
            }
        }
        assertThat(shared.metrics().succeeded()).isEqualTo(40_000);
    }

    @Test
    void aReadWaitsWhileAnUpdateIsInsideTheStore() throws Exception {
        Gate gate = new Gate();
        Stocks.Shared shared = Stocks.shared(gate);
        Thread writer = daemon("writer", () -> shared.stock().set("cork-board", 3));
        try {
            assertThat(gate.inside.await(10, TimeUnit.SECONDS)).as("the update reached the store").isTrue();
            Thread reader = daemon("reader", () -> shared.stock().level("cork-board"));
            awaitBlockedBehind(reader, writer);
            gate.letGo.countDown();
            reader.join(10_000);
            assertThat(reader.isAlive()).as("the read finished once the update had").isFalse();
        } finally {
            gate.letGo.countDown();
            writer.join(10_000);
        }
    }

    @Test
    void anUpdateTheStoreRefusesLeavesTheStockUsableForOtherThreads() throws Exception {
        Stocks.Shared shared = Stocks.shared(new Stocks.MapStock());
        ExecutorService first = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        ExecutorService second = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            Throwable refused = first.submit(() -> catchThrowable(() -> shared.stock().set("tin-lamp", -1)))
                    .get(10, TimeUnit.SECONDS);
            assertThat(refused).isInstanceOf(IllegalArgumentException.class);
            Future<Integer> next = second.submit(() -> {
                shared.stock().set("tin-lamp", 2);
                return shared.stock().level("tin-lamp");
            });
            try {
                assertThat(next.get(10, TimeUnit.SECONDS)).isEqualTo(2);
            } catch (TimeoutException e) {
                fail("after one refused update, another thread's calls never got in");
            }
        } finally {
            first.shutdownNow();
            second.shutdownNow();
        }
    }

    @Test
    void theBusyGaugeCountsCallsInTheStoreNotCallsWaitingForIt() throws Exception {
        Gate gate = new Gate();
        Stocks.Shared shared = Stocks.shared(gate);
        Thread writer = daemon("writer", () -> shared.stock().set("jute-rug", 8));
        try {
            assertThat(gate.inside.await(10, TimeUnit.SECONDS)).as("the update reached the store").isTrue();
            Thread reader = daemon("reader", () -> shared.stock().level("jute-rug"));
            awaitBlockedBehind(reader, writer);
            assertThat(shared.metrics().inFlight()).isEqualTo(1);
        } finally {
            gate.letGo.countDown();
            writer.join(10_000);
        }
    }
}
