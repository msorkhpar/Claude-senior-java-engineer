package practice;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class QuotaTest {

    /** A map whose first get() from one chosen thread reads the value, then waits until the test lets it go on. */
    static final class HoldingMap extends AbstractMap<String, Integer> {
        private final Map<String, Integer> inner = new HashMap<>();
        volatile Thread holds;
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch goOn = new CountDownLatch(1);

        @Override
        public Integer get(Object key) {
            Integer value = inner.get(key);
            if (Thread.currentThread() == holds) {
                holds = null;
                held.countDown();
                await(goOn);
            }
            return value;
        }

        @Override
        public Integer put(String key, Integer value) {
            return inner.put(key, value);
        }

        @Override
        public Set<Entry<String, Integer>> entrySet() {
            return inner.entrySet();
        }
    }

    /** Prices rows/100 + 1 units; the call from one chosen thread waits until the test lets it go on. */
    static final class HoldingPricing implements Quota.Pricing {
        volatile Thread holds;
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch goOn = new CountDownLatch(1);

        @Override
        public int unitsFor(int rows) {
            if (Thread.currentThread() == holds) {
                holds = null;
                held.countDown();
                await(goOn);
            }
            return rows / 100 + 1;
        }
    }

    interface Call {
        void run() throws Exception;
    }

    static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("latch not opened");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    static Thread thread(String name, Call call, AtomicReference<Throwable> failure) {
        Thread t = new Thread(() -> {
            try {
                call.run();
            } catch (Throwable e) {
                failure.compareAndSet(null, e);
            }
        }, name);
        t.setDaemon(true);
        return t;
    }

    /** Waits until second has finished, or waits for a lock first holds. */
    static void awaitDoneOrWaitingFor(Thread second, Thread first) {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (!second.isAlive()) return;
            ThreadInfo info = mx.getThreadInfo(second.threadId());
            if (info != null && info.getLockOwnerId() == first.threadId()) return;
            LockSupport.parkNanos(200_000);
        }
        fail(second.getName() + " neither finished nor waited for " + first.getName());
    }

    /** Runs first until it is held, runs second against it, then lets first go on. */
    static void interleave(Thread first, CountDownLatch firstHeld, CountDownLatch goOn, Thread second,
                           AtomicReference<Throwable> failure) throws InterruptedException {
        first.start();
        assertThat(firstHeld.await(10, TimeUnit.SECONDS)).as("first call reached the held point").isTrue();
        second.start();
        awaitDoneOrWaitingFor(second, first);
        goOn.countDown();
        first.join(10_000);
        second.join(10_000);
        assertThat(first.isAlive()).isFalse();
        assertThat(second.isAlive()).isFalse();
        assertThat(failure.get()).isNull();
    }

    @Test
    void searchAndExportChargeOneAndFiveUnits() {
        Quota quota = new Quota(new HashMap<>());
        quota.reset("t-harbor", 12);
        Quota.Api api = new Quota.Api(quota, rows -> rows / 100 + 1);

        assertThat(api.search("t-harbor")).isTrue();
        assertThat(api.export("t-harbor")).isTrue();

        assertThat(quota.remaining("t-harbor")).isEqualTo(6);
    }

    @Test
    void bulkChargesWhatPricingAsks() throws Exception {
        Quota quota = new Quota(new HashMap<>());
        quota.reset("t-larch", 40);
        Quota.Api api = new Quota.Api(quota, rows -> rows / 100 + 1);

        assertThat(api.bulk("t-larch", 1_250)).isTrue();

        assertThat(quota.remaining("t-larch")).isEqualTo(27);
    }

    @Test
    void aRequestCostingMoreThanIsLeftChargesNothing() throws Exception {
        Quota quota = new Quota(new HashMap<>());
        quota.reset("t-quill", 4);
        Quota.Api api = new Quota.Api(quota, rows -> rows / 100 + 1);

        assertThat(api.export("t-quill")).isFalse();
        assertThat(api.bulk("t-quill", 700)).isFalse();
        assertThat(quota.remaining("t-quill")).isEqualTo(4);
        assertThat(api.search("t-nobody")).isFalse();
    }

    @Test
    void anExportArrivingMidSearchIsStillCharged() throws Exception {
        HoldingMap counts = new HoldingMap();
        Quota quota = new Quota(counts);
        quota.reset("t-moss", 20);
        Quota.Api api = new Quota.Api(quota, rows -> rows / 100 + 1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread search = thread("search", () -> api.search("t-moss"), failure);
        Thread export = thread("export", () -> api.export("t-moss"), failure);
        counts.holds = search;

        interleave(search, counts.held, counts.goOn, export, failure);

        assertThat(quota.remaining("t-moss")).isEqualTo(14);
    }

    @Test
    void aSearchWhileBulkWaitsForPricingIsStillCharged() throws Exception {
        Quota quota = new Quota(new HashMap<>());
        quota.reset("t-fjord", 30);
        HoldingPricing pricing = new HoldingPricing();
        Quota.Api api = new Quota.Api(quota, pricing);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread bulk = thread("bulk", () -> api.bulk("t-fjord", 480), failure);
        Thread search = thread("search", () -> api.search("t-fjord"), failure);
        pricing.holds = bulk;

        interleave(bulk, pricing.held, pricing.goOn, search, failure);

        assertThat(quota.remaining("t-fjord")).isEqualTo(24);
    }

    @Test
    void aRefundArrivingMidSearchIsKept() throws Exception {
        HoldingMap counts = new HoldingMap();
        Quota quota = new Quota(counts);
        quota.reset("t-cedar", 9);
        Quota.Api api = new Quota.Api(quota, rows -> rows / 100 + 1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread search = thread("search", () -> api.search("t-cedar"), failure);
        Thread refund = thread("refund", () -> quota.refund("t-cedar", 7), failure);
        counts.holds = search;

        interleave(search, counts.held, counts.goOn, refund, failure);

        assertThat(quota.remaining("t-cedar")).isEqualTo(15);
    }
}
