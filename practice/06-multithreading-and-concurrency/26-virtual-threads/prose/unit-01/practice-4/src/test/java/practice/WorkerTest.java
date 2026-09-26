package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class WorkerTest {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final List<String> sink = new CopyOnWriteArrayList<>();
    private final AtomicInteger returned = new AtomicInteger(-1);
    private final AtomicBoolean stillInterrupted = new AtomicBoolean();
    private final CountDownLatch finished = new CountDownLatch(1);

    private Thread startWorker() {
        return Thread.ofVirtual().start(() -> {
            returned.set(Worker.drain(queue, sink::add));
            stillInterrupted.set(Thread.currentThread().isInterrupted());
            finished.countDown();
        });
    }

    /** Polls until the condition holds or 5 s pass; says whether it held. */
    private static boolean eventually(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                return false;
            }
            LockSupport.parkNanos(1_000_000);
        }
        return true;
    }

    private static boolean waitingOnTheQueue(Thread worker) {
        return eventually(() -> worker.getState() == Thread.State.WAITING);
    }

    @Test
    void handsOnEveryItemInOrder() throws InterruptedException {
        Thread worker = startWorker();
        try {
            queue.put("a");
            queue.put("b");
            queue.put("c");
            assertThat(eventually(() -> sink.size() == 3)).as("all three handed on").isTrue();
            assertThat(sink).containsExactly("a", "b", "c");
        } finally {
            worker.interrupt();
        }
    }

    @Test
    void anInterruptWhileWaitingEndsTheLoop() throws InterruptedException {
        Thread worker = startWorker();
        assertThat(waitingOnTheQueue(worker)).as("drain waits on the empty queue").isTrue();
        worker.interrupt();
        assertThat(finished.await(3, TimeUnit.SECONDS)).as("drain returned after the interrupt").isTrue();
        assertThat(returned.get()).isZero();
    }

    @Test
    void theInterruptStatusIsKept() throws InterruptedException {
        Thread worker = startWorker();
        queue.put("a");
        queue.put("b");
        assertThat(eventually(() -> sink.size() == 2)).as("both handed on").isTrue();
        assertThat(waitingOnTheQueue(worker)).as("drain waits for more").isTrue();
        worker.interrupt();
        assertThat(finished.await(5, TimeUnit.SECONDS)).as("drain returned after the interrupt").isTrue();
        assertThat(returned.get()).isEqualTo(2);
        assertThat(stillInterrupted.get()).as("interrupt status after drain returned").isTrue();
    }

    @Test
    void anAlreadyInterruptedDrainStopsAtOnce() throws Exception {
        LinkedBlockingQueue<String> ready = new LinkedBlockingQueue<>(List.of("a", "b"));
        List<String> handed = new CopyOnWriteArrayList<>();
        AtomicInteger count = new AtomicInteger(-1);
        AtomicBoolean flag = new AtomicBoolean();
        Thread worker = Thread.ofVirtual().start(() -> {
            Thread.currentThread().interrupt();
            count.set(Worker.drain(ready, handed::add));
            flag.set(Thread.currentThread().isInterrupted());
        });
        worker.join(5_000);
        assertThat(count.get()).as("a thread interrupted before drain starts hands nothing on").isZero();
        assertThat(handed).isEmpty();
        assertThat(flag.get()).as("and still has its interrupt status").isTrue();
    }
}
