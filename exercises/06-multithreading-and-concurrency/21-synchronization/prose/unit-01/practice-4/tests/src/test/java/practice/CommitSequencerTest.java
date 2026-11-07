package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class CommitSequencerTest {

    /** A worker that waits for its batch's turn and records the turn it saw when it went ahead. */
    static final class Worker {
        final Thread thread;
        final AtomicLong sawTurn = new AtomicLong(-1);

        Worker(CommitSequencer sequencer, long batch) {
            thread = new Thread(() -> {
                try {
                    sequencer.awaitTurn(batch);
                    sawTurn.set(sequencer.next());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "batch-" + batch);
            thread.setDaemon(true);
            thread.start();
        }

        boolean wentAhead() {
            return sawTurn.get() != -1;
        }
    }

    private final List<Thread> started = new ArrayList<>();

    Worker worker(CommitSequencer sequencer, long batch) {
        Worker w = new Worker(sequencer, batch);
        started.add(w.thread);
        return w;
    }

    @AfterEach
    void stopWorkers() throws InterruptedException {
        for (Thread t : started) t.interrupt();
        for (Thread t : started) t.join(10_000);
    }

    static ThreadInfo info(Thread t) {
        return ManagementFactory.getThreadMXBean().getThreadInfo(t.threadId());
    }

    /** Whether t is waiting for a notification on a monitor. */
    static boolean waitingOnAMonitor(Thread t) {
        ThreadInfo info = info(t);
        return info != null && info.getThreadState() == Thread.State.WAITING && info.getLockInfo() != null;
    }

    static void awaitUntil(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) Thread.onSpinWait();
    }

    @Test
    void aBatchWhoseTurnItIsGoesStraightAhead() throws InterruptedException {
        CommitSequencer sequencer = new CommitSequencer(41);

        sequencer.awaitTurn(41);
        sequencer.done(41);
        assertThat(sequencer.next()).isEqualTo(42);
        sequencer.awaitTurn(42);
        sequencer.done(42);

        assertThat(sequencer.next()).isEqualTo(43);
    }

    @Test
    void aWaitingBatchGoesAheadOnceTheOneBeforeIsDone() throws InterruptedException {
        CommitSequencer sequencer = new CommitSequencer(57);
        Worker later = worker(sequencer, 58);
        awaitUntil(() -> waitingOnAMonitor(later.thread));
        assertThat(later.wentAhead()).isFalse();

        sequencer.awaitTurn(57);
        sequencer.done(57);

        later.thread.join(10_000);
        assertThat(later.wentAhead()).isTrue();
        assertThat(later.sawTurn.get()).isEqualTo(58);
    }

    @Test
    void aBatchNeverGoesAheadOutOfTurn() {
        CommitSequencer sequencer = new CommitSequencer(11);
        Worker twelve = worker(sequencer, 12);
        Worker thirteen = worker(sequencer, 13);
        awaitUntil(() -> waitingOnAMonitor(twelve.thread) && waitingOnAMonitor(thirteen.thread));
        long waitsBefore = info(thirteen.thread).getWaitedCount();

        sequencer.done(11);

        awaitUntil(() -> thirteen.wentAhead() || info(thirteen.thread).getWaitedCount() > waitsBefore);
        assertThat(thirteen.wentAhead()).as("batch 13 went ahead while it was batch 12's turn").isFalse();
        awaitUntil(twelve::wentAhead);
        assertThat(twelve.sawTurn.get()).isEqualTo(12);
    }

    @Test
    void theNextBatchGoesAheadWhileALaterOneKeepsWaiting() throws InterruptedException {
        CommitSequencer sequencer = new CommitSequencer(21);
        Worker twentyThree = worker(sequencer, 23);
        awaitUntil(() -> waitingOnAMonitor(twentyThree.thread));
        Worker twentyTwo = worker(sequencer, 22);
        awaitUntil(() -> waitingOnAMonitor(twentyTwo.thread));

        sequencer.done(21);

        twentyTwo.thread.join(10_000);
        assertThat(twentyTwo.wentAhead()).as("batch 22 went ahead after batch 21 was done").isTrue();
        assertThat(twentyThree.wentAhead()).isFalse();
    }
}
