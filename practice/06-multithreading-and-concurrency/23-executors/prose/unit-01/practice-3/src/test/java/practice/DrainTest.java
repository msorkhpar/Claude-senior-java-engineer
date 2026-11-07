package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrainTest {

    private final ExecutorService pool = Executors.newSingleThreadExecutor(daemon("invoice-worker"));
    private final AtomicReference<Thread> deployerThread = new AtomicReference<>();
    private final ExecutorService deployer = Executors.newSingleThreadExecutor(r -> {
        Thread t = daemon("deployer").newThread(r);
        deployerThread.set(t);
        return t;
    });
    private final CountDownLatch never = new CountDownLatch(1);
    private final AtomicInteger ran = new AtomicInteger();
    private final AtomicBoolean interrupted = new AtomicBoolean();

    private static java.util.concurrent.ThreadFactory daemon(String name) {
        return r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        };
    }

    @AfterEach
    void cleanUp() {
        never.countDown();
        pool.shutdownNow();
        deployer.shutdownNow();
    }

    /** A task that runs until the latch opens, and stops early if interrupted. */
    private Runnable invoiceRun(CountDownLatch until) {
        return () -> {
            try {
                until.await();
                ran.incrementAndGet();
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        };
    }

    /** Calls stop() on the deployer thread; returns what it returned and whether its flag was set after. */
    private record Stopped(List<Runnable> neverStarted, boolean flagAfter) { }

    private Future<Stopped> stopOnDeployer(long grace, TimeUnit unit) {
        return deployer.submit(() -> {
            List<Runnable> left = Drain.stop(pool, grace, unit);
            return new Stopped(left, Thread.currentThread().isInterrupted());
        });
    }

    private void awaitShutdownStarted() {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!pool.isShutdown() && System.nanoTime() < end) Thread.onSpinWait();
        assertThat(pool.isShutdown()).as("stop() shut the pool down").isTrue();
    }

    @Test
    void anIdlePoolStopsAndHandsNothingBack() throws Exception {
        pool.submit(ran::incrementAndGet).get(5, TimeUnit.SECONDS);

        Stopped stopped = stopOnDeployer(5, TimeUnit.SECONDS).get(10, TimeUnit.SECONDS);

        assertThat(stopped.neverStarted()).isEmpty();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).as("pool terminated").isTrue();
    }

    @Test
    void queuedWorkThatEndsWithinTheGraceStillRuns() throws Exception {
        CountDownLatch go = new CountDownLatch(1);
        pool.execute(invoiceRun(go));
        pool.execute(invoiceRun(go));
        pool.execute(invoiceRun(go));

        Future<Stopped> stop = stopOnDeployer(30, TimeUnit.SECONDS);
        awaitShutdownStarted();
        go.countDown();
        Stopped stopped = stop.get(10, TimeUnit.SECONDS);

        assertThat(ran).as("invoice runs completed").hasValue(3);
        assertThat(stopped.neverStarted()).isEmpty();
        assertThat(interrupted).isFalse();
    }

    @Test
    void stopReturnsOnlyOnceTheWorkHasEnded() throws Exception {
        CountDownLatch go = new CountDownLatch(1);
        pool.execute(invoiceRun(go));

        Future<Stopped> stop = stopOnDeployer(30, TimeUnit.SECONDS);
        awaitShutdownStarted();

        assertThatThrownBy(() -> stop.get(1, TimeUnit.SECONDS))
                .as("stop() returned while an invoice run was still going").isInstanceOf(TimeoutException.class);
        go.countDown();
        stop.get(10, TimeUnit.SECONDS);
        assertThat(ran).hasValue(1);
    }

    @Test
    void workThatOutlivesTheGraceIsInterruptedAndTheQueueHandedBack() throws Exception {
        Runnable stuck = invoiceRun(never);
        Runnable queued1 = invoiceRun(never);
        Runnable queued2 = invoiceRun(never);
        pool.execute(stuck);
        pool.execute(queued1);
        pool.execute(queued2);

        Stopped stopped = stopOnDeployer(300, TimeUnit.MILLISECONDS).get(10, TimeUnit.SECONDS);

        assertThat(stopped.neverStarted()).containsExactly(queued1, queued2);
        assertThat(interrupted).as("the stuck run was interrupted").isTrue();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).as("pool terminated").isTrue();
    }

    @Test
    void aDeployerInterruptedWhileItWaitsStopsThePoolAndKeepsItsFlag() throws Exception {
        pool.execute(invoiceRun(never));
        Runnable queued = invoiceRun(never);
        pool.execute(queued);

        Future<Stopped> stop = stopOnDeployer(10, TimeUnit.MINUTES);
        awaitShutdownStarted();
        deployerThread.get().interrupt();                    // the orchestrator gives up waiting
        Stopped stopped = stop.get(10, TimeUnit.SECONDS);

        assertThat(stopped.flagAfter()).as("the deployer's interrupt flag after stop()").isTrue();
        assertThat(stopped.neverStarted()).containsExactly(queued);
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).as("pool terminated").isTrue();
    }
}
