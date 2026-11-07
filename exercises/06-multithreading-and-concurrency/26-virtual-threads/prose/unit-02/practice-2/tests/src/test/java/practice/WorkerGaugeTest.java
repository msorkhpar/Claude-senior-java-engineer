package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class WorkerGaugeTest {

    /** Polls until the condition holds or 5 s pass; says whether it held. */
    private static boolean eventually(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) return false;
            LockSupport.parkNanos(1_000_000);
        }
        return true;
    }

    @Test
    void runsEachRequestOnANamedVirtualThread() throws Exception {
        WorkerGauge gauge = new WorkerGauge("checkout-");
        Set<Thread> seen = ConcurrentHashMap.newKeySet();
        CountDownLatch ran = new CountDownLatch(4);
        try (ExecutorService executor = Executors.newThreadPerTaskExecutor(gauge)) {
            for (int i = 0; i < 4; i++) {
                executor.submit(() -> { seen.add(Thread.currentThread()); ran.countDown(); });
            }
            assertThat(ran.await(10, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(seen).hasSize(4).allSatisfy(t -> {
            assertThat(t.isVirtual()).isTrue();
            assertThat(t.getName()).startsWith("checkout-");
        });
    }

    @Test
    void countsTheRequestsInProgress() throws Exception {
        WorkerGauge gauge = new WorkerGauge("checkout-");
        CountDownLatch gate = new CountDownLatch(1);
        CountDownLatch started = new CountDownLatch(5);
        try (ExecutorService executor = Executors.newThreadPerTaskExecutor(gauge)) {
            try {
                for (int i = 0; i < 5; i++) {
                    executor.submit(() -> { started.countDown(); gate.await(15, TimeUnit.SECONDS); return null; });
                }
                assertThat(started.await(10, TimeUnit.SECONDS)).isTrue();
                assertThat(eventually(() -> gauge.busy() == 5)).as("busy() while five requests wait").isTrue();
            } finally {
                gate.countDown();
            }
        }
        assertThat(eventually(() -> gauge.busy() == 0)).as("busy() once they are done").isTrue();
    }

    @Test
    void aRequestThatFailsIsNoLongerBusy() throws Exception {
        WorkerGauge gauge = new WorkerGauge("checkout-");
        Thread t = gauge.newThread(() -> { throw new IllegalStateException("card declined"); });
        t.setUncaughtExceptionHandler((thread, e) -> { });
        t.start();
        assertThat(t.join(java.time.Duration.ofSeconds(10))).isTrue();
        assertThat(gauge.busy()).isZero();
    }

    @Test
    void aThreadNotYetStartedIsNotBusy() {
        WorkerGauge gauge = new WorkerGauge("checkout-");
        gauge.newThread(() -> { });
        gauge.newThread(() -> { });
        gauge.newThread(() -> { });
        assertThat(gauge.busy()).isZero();
    }
}
