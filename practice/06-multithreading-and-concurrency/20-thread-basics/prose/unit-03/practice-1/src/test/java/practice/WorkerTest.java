package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class WorkerTest {

    /**
     * Runs countSteps on a daemon thread. Step 5 signals the test and then spins, at most 5 s,
     * until its own thread has been interrupted; the test interrupts it once step 5 has begun.
     */
    private static long[] interruptDuringStepFive() throws InterruptedException {
        CountDownLatch inStepFive = new CountDownLatch(1);
        AtomicLong result = new AtomicLong(-1);
        AtomicBoolean flagAfter = new AtomicBoolean();
        Thread worker = new Thread(() -> {
            result.set(Worker.countSteps(n -> {
                if (n == 5) {
                    inStepFive.countDown();
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                    while (!Thread.currentThread().isInterrupted() && System.nanoTime() < deadline) {
                        Thread.onSpinWait();
                    }
                }
            }));
            flagAfter.set(Thread.currentThread().isInterrupted());
        });
        worker.setDaemon(true);
        worker.start();
        assertThat(inStepFive.await(5, TimeUnit.SECONDS)).as("step 5 began").isTrue();
        worker.interrupt();
        worker.join(5_000);
        assertThat(worker.isAlive()).as("the worker stopped after the interrupt").isFalse();
        return new long[] {result.get(), flagAfter.get() ? 1 : 0};
    }

    @Test
    void stopsAfterTheStepDuringWhichItWasInterrupted() throws InterruptedException {
        assertThat(interruptDuringStepFive()[0]).isEqualTo(5);
    }

    @Test
    void leavesTheInterruptFlagSet() throws InterruptedException {
        long[] seen = interruptDuringStepFive();
        assertThat(seen[0]).isEqualTo(5);
        assertThat(seen[1]).as("the interrupt flag is still set after countSteps returns").isEqualTo(1);
    }

    @Test
    void anInterruptedThreadDoesNoWork() {
        AtomicInteger calls = new AtomicInteger();
        Thread.currentThread().interrupt();
        long result;
        try {
            result = Worker.countSteps(n -> calls.incrementAndGet());
        } finally {
            Thread.interrupted();
        }
        assertThat(result).isZero();
        assertThat(calls.get()).isZero();
    }
}
