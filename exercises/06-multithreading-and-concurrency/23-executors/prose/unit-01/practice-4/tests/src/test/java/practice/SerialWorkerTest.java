package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SerialWorkerTest {

    @Test
    void runsATaskAndReturnsItsResult() throws Exception {
        try (SerialWorker worker = new SerialWorker("audit")) {
            assertThat(worker.submit(() -> 6 * 7).get(5, TimeUnit.SECONDS)).isEqualTo(42);
        }
    }

    @Test
    void runsEveryTaskOnOneThreadInOrder() throws Exception {
        List<Integer> order = new CopyOnWriteArrayList<>();
        Set<Thread> threads = ConcurrentHashMap.newKeySet();
        CountDownLatch gate = new CountDownLatch(1);
        try (SerialWorker worker = new SerialWorker("audit")) {
            worker.submit(() -> {
                gate.await(5, TimeUnit.SECONDS);
                threads.add(Thread.currentThread());
                return order.add(1);
            });
            for (int i = 2; i <= 5; i++) {
                int n = i;
                worker.submit(() -> {
                    threads.add(Thread.currentThread());
                    return order.add(n);
                });
            }
            gate.countDown();
            worker.submit(() -> true).get(5, TimeUnit.SECONDS);
        }
        assertThat(threads).as("threads that ran the tasks").hasSize(1);
        assertThat(order).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void closeWaitsForQueuedTasks() throws InterruptedException {
        List<String> ran = new CopyOnWriteArrayList<>();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch gate = new CountDownLatch(1);
        SerialWorker worker = new SerialWorker("audit");
        worker.submit(() -> {
            started.countDown();
            gate.await(5, TimeUnit.SECONDS);
            return ran.add("slow");
        });
        worker.submit(() -> ran.add("queued"));
        assertThat(started.await(5, TimeUnit.SECONDS)).as("the slow task started").isTrue();
        Thread closer = new Thread(worker::close);
        closer.setDaemon(true);
        closer.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (closer.isAlive() && closer.getState() != Thread.State.WAITING
                && closer.getState() != Thread.State.TIMED_WAITING && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
        assertThat(closer.isAlive()).as("close() still waits while a task is pending").isTrue();
        gate.countDown();
        closer.join(5_000);
        assertThat(closer.isAlive()).as("close() returned").isFalse();
        assertThat(ran).containsExactly("slow", "queued");
    }

    @Test
    void itsThreadIsNamed() throws Exception {
        try (SerialWorker worker = new SerialWorker("audit")) {
            assertThat(worker.submit(() -> Thread.currentThread().getName()).get(5, TimeUnit.SECONDS)).isEqualTo("audit");
            assertThat(worker.submit(() -> Thread.currentThread().isDaemon()).get(5, TimeUnit.SECONDS)).isTrue();
        }
    }
}
