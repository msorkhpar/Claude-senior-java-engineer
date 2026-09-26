package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LatchWaiterTest {

    /** Waits, at most 5 s, until the thread exists and is parked (WAITING); returns whether it is. */
    private static boolean parks(AtomicReference<Thread> thread) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            Thread t = thread.get();
            if (t != null && t.getState() == Thread.State.WAITING) {
                return true;
            }
            Thread.sleep(1);
        }
        return false;
    }

    @Test
    void passesAnOpenLatch() throws InterruptedException {
        LatchWaiter.await(new CountDownLatch(0));
        CountDownLatch opened = new CountDownLatch(1);
        opened.countDown();
        LatchWaiter.await(opened);
    }

    @Test
    void anotherTaskOfAOneThreadPoolCanOpenTheLatch() throws Exception {
        ForkJoinPool pool = new ForkJoinPool(1);
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Thread> waiter = new AtomicReference<>();
            ForkJoinTask<Boolean> waiting = pool.submit(() -> {
                waiter.set(Thread.currentThread());
                LatchWaiter.await(latch);
                return true;
            });
            assertThat(parks(waiter)).as("task A waits on the closed latch").isTrue();
            ForkJoinTask<?> opener = pool.submit(latch::countDown);
            try {
                opener.get(3, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                fail("task B never ran: the pool's only worker is blocked in await");
            }
            assertThat(waiting.get(3, TimeUnit.SECONDS)).isTrue();
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void aWaitCanBeInterrupted() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Thread> waiter = new AtomicReference<>();
        AtomicReference<Object> outcome = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            waiter.set(Thread.currentThread());
            try {
                LatchWaiter.await(latch);
                outcome.set("returned");
            } catch (Throwable t) {
                outcome.set(t);
            }
        });
        thread.setDaemon(true);
        thread.start();
        assertThat(parks(waiter)).as("await waits on the closed latch").isTrue();
        thread.interrupt();
        thread.join(5_000);
        assertThat(thread.isAlive()).as("the interrupted await is still waiting").isFalse();
        assertThat(outcome.get()).isInstanceOf(InterruptedException.class);
    }
}
