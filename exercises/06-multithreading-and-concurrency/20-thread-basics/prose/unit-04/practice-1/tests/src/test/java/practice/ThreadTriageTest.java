package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import practice.ThreadTriage.Activity;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadTriageTest {

    private static final ThreadMXBean MX = ManagementFactory.getThreadMXBean();

    /** Every thread a test starts waits on this, directly or behind a holder, until the test ends. */
    private final CountDownLatch release = new CountDownLatch(1);
    private final List<Thread> started = new ArrayList<>();

    @AfterEach
    void letEveryThreadGo() throws InterruptedException {
        release.countDown();
        for (Thread t : started) t.interrupt();
        for (Thread t : started) t.join(5_000);
    }

    private interface Body { void run() throws Exception; }

    private Thread daemon(String name, Body body) {
        Thread t = new Thread(() -> {
            try { body.run(); } catch (Exception e) { /* interrupted at the end of the test */ }
        }, name);
        t.setDaemon(true);
        started.add(t);
        t.start();
        return t;
    }

    /** A thread that holds whatever {@code take} takes, and keeps computing until the test ends. */
    private Thread holder(String name, Body take, CountDownLatch holding) {
        return daemon(name, () -> {
            take.run();
            holding.countDown();
            while (release.getCount() > 0 && !Thread.currentThread().isInterrupted()) Thread.onSpinWait();
        });
    }

    /**
     * Takes snapshots of the given threads until every one of them is where the test put it,
     * and returns that snapshot (at most ten seconds).
     */
    private static ThreadInfo[] snapshotOnce(Map<Thread, Predicate<ThreadInfo>> expected) {
        List<Thread> threads = new ArrayList<>(expected.keySet());
        long[] ids = threads.stream().mapToLong(Thread::threadId).toArray();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (true) {
            ThreadInfo[] infos = MX.getThreadInfo(ids);
            boolean all = true;
            for (int i = 0; i < infos.length; i++) {
                if (infos[i] == null || !expected.get(threads.get(i)).test(infos[i])) all = false;
            }
            if (all) return infos;
            if (System.nanoTime() > deadline) throw new AssertionError("the test's threads never got where it put them");
            Thread.yield();
        }
    }

    private static Predicate<ThreadInfo> in(Thread.State state) {
        return info -> info.getThreadState() == state;
    }

    private static Predicate<ThreadInfo> waitingBehind(Thread.State state, Thread owner) {
        return info -> info.getThreadState() == state && info.getLockOwnerId() == owner.threadId();
    }

    private static Predicate<ThreadInfo> parkedIn(Thread.State state, String lockClass) {
        return info -> info.getThreadState() == state && info.getLockInfo() != null
                && info.getLockInfo().getClassName().endsWith(lockClass) && info.getLockOwnerId() == -1;
    }

    @Test
    void sortsAPileUpOnASynchronizedLedgerAndTheThreadsAroundIt() throws InterruptedException {
        Object ledger = new Object();
        CountDownLatch holding = new CountDownLatch(1);
        Thread writer = daemon("ledger-writer", () -> {
            synchronized (ledger) {
                holding.countDown();
                while (release.getCount() > 0 && !Thread.currentThread().isInterrupted()) Thread.onSpinWait();
            }
        });
        assertThat(holding.await(5, TimeUnit.SECONDS)).isTrue();
        Map<Thread, Predicate<ThreadInfo>> expected = new LinkedHashMap<>();
        expected.put(writer, in(Thread.State.RUNNABLE));
        for (String name : List.of("payment-1", "payment-2", "payment-3")) {
            expected.put(daemon(name, () -> { synchronized (ledger) { } }), waitingBehind(Thread.State.BLOCKED, writer));
        }
        expected.put(daemon("audit-pause", () -> Thread.sleep(120_000)), in(Thread.State.TIMED_WAITING));
        ThreadInfo[] snapshot = snapshotOnce(expected);

        assertThat(ThreadTriage.summary(snapshot)).isEqualTo(Map.of(
                Activity.ACTIVE, 1, Activity.IDLE, 1, Activity.STUCK_BEHIND_ANOTHER, 3));
        assertThat(ThreadTriage.stuckBehind(snapshot)).isEqualTo(Map.of("ledger-writer", 3));
    }

    @Test
    void requestsWaitingForAnExplicitLockAreStuckBehindItsHolder() throws InterruptedException {
        ReentrantLock rates = new ReentrantLock();
        CountDownLatch holding = new CountDownLatch(1);
        Thread loader = holder("rates-loader", rates::lock, holding);
        assertThat(holding.await(5, TimeUnit.SECONDS)).isTrue();
        Map<Thread, Predicate<ThreadInfo>> expected = new LinkedHashMap<>();
        expected.put(loader, in(Thread.State.RUNNABLE));
        for (String name : List.of("quote-1", "quote-2")) {
            expected.put(daemon(name, rates::lockInterruptibly), waitingBehind(Thread.State.WAITING, loader));
        }
        ThreadInfo[] snapshot = snapshotOnce(expected);

        assertThat(ThreadTriage.summary(snapshot)).isEqualTo(Map.of(
                Activity.ACTIVE, 1, Activity.IDLE, 0, Activity.STUCK_BEHIND_ANOTHER, 2));
        assertThat(ThreadTriage.stuckBehind(snapshot)).isEqualTo(Map.of("rates-loader", 2));
    }

    @Test
    void workersWaitingForJobsAreIdle() {
        BlockingQueue<String> jobs = new LinkedBlockingQueue<>();
        BlockingQueue<String> uploads = new ArrayBlockingQueue<>(4);
        Map<Thread, Predicate<ThreadInfo>> expected = new LinkedHashMap<>();
        expected.put(daemon("mailer-1", jobs::take), parkedIn(Thread.State.WAITING, "ConditionObject"));
        expected.put(daemon("mailer-2", jobs::take), parkedIn(Thread.State.WAITING, "ConditionObject"));
        expected.put(daemon("uploader", () -> uploads.poll(2, TimeUnit.MINUTES)), parkedIn(Thread.State.TIMED_WAITING, "ConditionObject"));
        expected.put(daemon("warm-up", release::await), parkedIn(Thread.State.WAITING, "Sync"));
        ThreadInfo[] snapshot = snapshotOnce(expected);

        assertThat(ThreadTriage.summary(snapshot)).isEqualTo(Map.of(
                Activity.ACTIVE, 0, Activity.IDLE, 4, Activity.STUCK_BEHIND_ANOTHER, 0));
        assertThat(ThreadTriage.stuckBehind(snapshot)).isEmpty();
    }

    @Test
    void aRequestThatWillGiveUpAfterATimeoutIsStuckUntilThen() throws InterruptedException {
        ReentrantLock seats = new ReentrantLock();
        CountDownLatch holding = new CountDownLatch(1);
        Thread booking = holder("seat-booking", seats::lock, holding);
        assertThat(holding.await(5, TimeUnit.SECONDS)).isTrue();
        Map<Thread, Predicate<ThreadInfo>> expected = new LinkedHashMap<>();
        expected.put(booking, in(Thread.State.RUNNABLE));
        expected.put(daemon("checkout-9", () -> seats.tryLock(2, TimeUnit.MINUTES)),
                waitingBehind(Thread.State.TIMED_WAITING, booking));
        ThreadInfo[] snapshot = snapshotOnce(expected);

        assertThat(ThreadTriage.summary(snapshot)).isEqualTo(Map.of(
                Activity.ACTIVE, 1, Activity.IDLE, 0, Activity.STUCK_BEHIND_ANOTHER, 1));
        assertThat(ThreadTriage.stuckBehind(snapshot)).isEqualTo(Map.of("seat-booking", 1));
    }
}
