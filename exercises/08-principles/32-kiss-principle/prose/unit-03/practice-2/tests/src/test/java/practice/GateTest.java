package practice;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GateTest {

    private static final ThreadMXBean MX = ManagementFactory.getThreadMXBean();

    private int appended;                                  // touched only inside the gate

    @Test
    void returnsWhatTheJobReturns() throws Exception {
        Gate gate = new Gate();

        assertThat(on("settle-apac", () -> gate.inside(() -> "batch 0417 settled")).get(5, TimeUnit.SECONDS))
                .isEqualTo("batch 0417 settled");
    }

    @Test
    void jobsFromManyThreadsRunOneAtATime() throws Exception {
        Gate gate = new Gate();
        List<FutureTask<Void>> jobs = new ArrayList<>();
        for (int t = 0; t < 4; t++) {
            jobs.add(on("settle-" + t, () -> {
                for (int i = 0; i < 5_000; i++) gate.inside(() -> appended++);
                return null;
            }));
        }
        for (FutureTask<Void> job : jobs) job.get(10, TimeUnit.SECONDS);

        assertThat(on("auditor", () -> gate.inside(() -> appended)).get(5, TimeUnit.SECONDS)).isEqualTo(20_000);
    }

    @Test
    void aJobWaitingAtTheGateNamesTheJobInside() throws Exception {
        Gate gate = new Gate();
        CountDownLatch inside = new CountDownLatch(1), finish = new CountDownLatch(1);
        FutureTask<String> holder = on("settle-eu", () -> gate.inside(() -> {
            inside.countDown();
            await(finish);
            return "eu";
        }));
        try {
            assertThat(inside.await(5, TimeUnit.SECONDS)).as("settle-eu got inside").isTrue();
            FutureTask<String> waiter = on("settle-us", () -> gate.inside(() -> "us"));
            Thread waiterThread = threadNamed("settle-us");

            String owner = null;
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (owner == null && System.nanoTime() < deadline) {
                ThreadInfo info = MX.getThreadInfo(waiterThread.threadId());
                if (info != null) owner = info.getLockOwnerName();
                Thread.onSpinWait();
            }
            assertThat(owner).as("the holder ThreadMXBean reports for settle-us while it waits").isEqualTo("settle-eu");

            finish.countDown();
            assertThat(waiter.get(5, TimeUnit.SECONDS)).isEqualTo("us");
            assertThat(holder.get(5, TimeUnit.SECONDS)).isEqualTo("eu");
        } finally {
            finish.countDown();
        }
    }

    @Test
    void aJobThatFailsDoesNotKeepTheGateClosed() throws Exception {
        Gate gate = new Gate();
        FutureTask<String> failing = on("settle-latam", () -> gate.inside(() -> {
            throw new IllegalArgumentException("ledger line 88 has no currency");
        }));
        assertThatThrownBy(() -> failing.get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .cause().isInstanceOf(IllegalArgumentException.class).hasMessage("ledger line 88 has no currency");

        FutureTask<String> next = on("settle-mena", () -> gate.inside(() -> "mena"));
        assertThat(next.get(5, TimeUnit.SECONDS)).as("the next job, on another thread, gets in").isEqualTo("mena");
    }

    private final List<Thread> started = new ArrayList<>();

    private <T> FutureTask<T> on(String name, Callable<T> body) {
        FutureTask<T> task = new FutureTask<>(body);
        Thread t = new Thread(task, name);
        t.setDaemon(true);
        started.add(t);
        t.start();
        return task;
    }

    private Thread threadNamed(String name) {
        return started.stream().filter(t -> t.getName().equals(name)).findFirst().orElseThrow();
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
