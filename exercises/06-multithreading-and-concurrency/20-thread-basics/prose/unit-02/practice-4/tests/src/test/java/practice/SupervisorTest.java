package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SupervisorTest {

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Waits, at most 5 s, until the thread is parked in a timed wait. */
    private static boolean parked(Thread t) {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < end) {
            if (t.getState() == Thread.State.TIMED_WAITING) return true;
            Thread.onSpinWait();
        }
        return false;
    }

    @Test
    void theFirstCheckPutsTheConsumerToWork() throws InterruptedException {
        CountDownLatch working = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Supervisor supervisor = new Supervisor("payments-consumer", () -> {
            working.countDown();
            awaitQuietly(release);
        });
        try {
            supervisor.check();

            assertThat(working.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(supervisor.worker().getName()).isEqualTo("payments-consumer");
        } finally {
            release.countDown();
        }
    }

    @Test
    void putsAConsumerThatDiedBackToWork() throws InterruptedException {
        AtomicInteger runs = new AtomicInteger();
        CountDownLatch backAtWork = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Supervisor supervisor = new Supervisor("refunds-consumer", () -> {
            if (runs.incrementAndGet() == 1) {
                throw new IllegalArgumentException("refund 7731: currency XTS not supported");
            }
            backAtWork.countDown();
            awaitQuietly(release);
        });
        try {
            supervisor.check();
            Thread first = supervisor.worker();
            first.join(5_000);
            assertThat(first.isAlive()).isFalse();

            supervisor.check();

            assertThat(backAtWork.await(5, TimeUnit.SECONDS)).as("the consumer ran again").isTrue();
            assertThat(supervisor.worker().getName()).isEqualTo("refunds-consumer");
        } finally {
            release.countDown();
        }
    }

    @Test
    void leavesAConsumerThatIsWaitingForWorkAlone() throws InterruptedException {
        AtomicInteger runs = new AtomicInteger();
        CountDownLatch nextPayment = new CountDownLatch(1);
        Supervisor supervisor = new Supervisor("invoices-consumer", () -> {
            runs.incrementAndGet();
            try {
                nextPayment.await(10, TimeUnit.SECONDS);   // an empty queue: waiting for work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        try {
            supervisor.check();
            assertThat(parked(supervisor.worker())).as("the consumer is waiting for work").isTrue();

            supervisor.check();
        } finally {
            nextPayment.countDown();
        }
        supervisor.worker().join(5_000);

        assertThat(runs.get()).as("consumers started").isEqualTo(1);
    }
}
