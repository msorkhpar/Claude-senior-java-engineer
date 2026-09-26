package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class JobsTest {

    private static ExecutorService singleDaemonThread() {
        return Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
    }

    /** Submits the task, waits until its first step, cancels it, then asks the same executor for 42. */
    private static Integer cancelThenRunAnother(long pauseMillis) throws Exception {
        ExecutorService executor = singleDaemonThread();
        try {
            CountDownLatch firstStep = new CountDownLatch(1);
            AtomicReference<Thread> worker = new AtomicReference<>();
            Future<Integer> future = executor.submit(Jobs.countingTask(Integer.MAX_VALUE, pauseMillis, i -> {
                worker.set(Thread.currentThread());
                firstStep.countDown();
            }));
            assertThat(firstStep.await(5, TimeUnit.SECONDS)).as("the task began").isTrue();
            if (pauseMillis > 0) {
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (worker.get().getState() != Thread.State.TIMED_WAITING && System.nanoTime() < deadline) {
                    Thread.sleep(1);
                }
            }
            future.cancel(true);
            try {
                return executor.submit(() -> 42).get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                return null;
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void countsToTheLimit() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = singleDaemonThread();
        try {
            assertThat(executor.submit(Jobs.countingTask(10, 0, i -> { })).get(5, TimeUnit.SECONDS)).isEqualTo(10);
            assertThat(executor.submit(Jobs.countingTask(3, 1, i -> { })).get(5, TimeUnit.SECONDS)).isEqualTo(3);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void cancelFreesTheWorker() throws Exception {
        assertThat(cancelThenRunAnother(0)).as("the executor ran the next task").isEqualTo(42);
    }

    @Test
    void cancelWakesASleepingTask() throws Exception {
        assertThat(cancelThenRunAnother(60_000)).as("the executor ran the next task").isEqualTo(42);
    }
}
