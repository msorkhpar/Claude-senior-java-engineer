package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PipelineTest {

    @Test
    void transformsEveryItem() {
        assertThat(Pipeline.run(List.of("a", "b", "c", "d"), String::toUpperCase, 1))
                .containsExactlyInAnyOrder("A", "B", "C", "D");
    }

    /** Each stage call arrives, then waits (at most 2 s) until three calls are in progress. */
    @Test
    void workersRunAtTheSameTime() {
        CountDownLatch inProgress = new CountDownLatch(3);
        List<String> results = Pipeline.run(List.of("x", "y", "z"), item -> {
            inProgress.countDown();
            try {
                return inProgress.await(2, TimeUnit.SECONDS) ? item : "alone";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "interrupted";
            }
        }, 3);
        assertThat(results).as("stage calls that never met the other two").containsExactlyInAnyOrder("x", "y", "z");
    }

    /**
     * The one stage call blocks until the test opens a gate. After it starts, the test gives run 3 s to
     * return early (a run that waits for its workers cannot), then opens the gate.
     */
    @Test
    void returnsOnlyWhenTheWorkIsDone() throws InterruptedException {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch gate = new CountDownLatch(1);
        AtomicReference<List<String>> results = new AtomicReference<>();
        Thread caller = Thread.ofPlatform().daemon(true).start(() -> results.set(Pipeline.run(List.of("slow"), s -> {
            entered.countDown();
            try {
                gate.await(8, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return s + "!";
        }, 2)));
        try {
            assertThat(entered.await(5, TimeUnit.SECONDS)).as("the stage call started").isTrue();
            caller.join(3_000);
            assertThat(caller.isAlive()).as("run is still waiting for the stage call").isTrue();
        } finally {
            gate.countDown();
        }
        caller.join(5_000);
        assertThat(results.get()).containsExactly("slow!");
    }

    /** Runs the pipeline on its own thread, so one that never ends cannot hang the test. */
    @Test
    void everyWorkerStops() throws InterruptedException {
        AtomicReference<List<String>> results = new AtomicReference<>();
        Thread caller = Thread.ofPlatform().daemon(true)
                .start(() -> results.set(Pipeline.run(List.of("a"), s -> s, 3)));
        caller.join(5_000);
        assertThat(caller.isAlive()).as("run is still waiting for its workers").isFalse();
        assertThat(results.get()).containsExactly("a");
    }

    @Test
    void anEmptyItemIsNotThePill() {
        List<String> results = Pipeline.run(List.of(new String(""), new String("a")), s -> "<" + s + ">", 2);
        assertThat(results).as("an empty item is an item, not the end marker").containsExactlyInAnyOrder("<>", "<a>");
    }

    @Test
    void noWorkerIsLeftWaiting() {
        CountDownLatch inProgress = new CountDownLatch(3);
        Set<Thread> workers = ConcurrentHashMap.newKeySet();
        List<String> results = Pipeline.run(List.of("x", "y", "z"), item -> {
            workers.add(Thread.currentThread());
            inProgress.countDown();
            try {
                inProgress.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return item;
        }, 3);
        assertThat(results).containsExactlyInAnyOrder("x", "y", "z");
        assertThat(workers).hasSize(3);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (workers.stream().anyMatch(Thread::isAlive) && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertThat(workers).as("every worker thread has ended once run returns, none left waiting on the queue")
                .noneMatch(Thread::isAlive);
    }

    @Test
    void workersAreVirtualThreads() {
        Set<Boolean> virtual = ConcurrentHashMap.newKeySet();
        Pipeline.run(List.of("a", "b", "c"), s -> {
            virtual.add(Thread.currentThread().isVirtual());
            return s;
        }, 2);
        assertThat(virtual).as("the workers are virtual threads").containsExactly(true);
    }
}
