package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class GatherTest {

    private ExecutorService executor;

    @AfterEach
    void stop() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void gathersEveryResult() throws InterruptedException {
        executor = Executors.newSingleThreadExecutor();
        List<Callable<Integer>> tasks = List.of(() -> 1, () -> 4, () -> 9);
        assertThat(Gather.all(executor, tasks, 5, TimeUnit.SECONDS, -1)).containsExactly(1, 4, 9);
    }

    /** An executor that only holds what it is given, so the test decides when, and in what order, each task runs. */
    private static final class HeldExecutor extends AbstractExecutorService {
        final BlockingQueue<Runnable> held = new LinkedBlockingQueue<>();

        @Override
        public void execute(Runnable command) {
            held.add(command);
        }

        @Override
        public void shutdown() {
        }

        @Override
        public List<Runnable> shutdownNow() {
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }
    }

    @Test
    void keepsInputOrderWhenLaterTasksFinishFirst() throws InterruptedException {
        HeldExecutor held = new HeldExecutor();
        executor = held;
        List<Callable<String>> tasks = List.of(() -> "first", () -> "second", () -> "third");
        AtomicReference<List<String>> results = new AtomicReference<>();
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread caller = new Thread(() -> {
            try {
                results.set(Gather.all(held, tasks, 8, TimeUnit.SECONDS, "none"));
            } catch (Throwable t) {
                thrown.set(t);
            }
        });
        caller.setDaemon(true);
        caller.start();
        List<Runnable> submitted = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Runnable next = held.held.poll(5, TimeUnit.SECONDS);
            assertThat(next).as("task %d was submitted", i + 1).isNotNull();
            submitted.add(next);
        }
        for (int i = submitted.size() - 1; i >= 0; i--) {
            submitted.get(i).run();
        }
        caller.join(5_000);
        assertThat(caller.isAlive()).as("all returned").isFalse();
        assertThat(thrown.get()).isNull();
        assertThat(results.get()).containsExactly("first", "second", "third");
    }

    @Test
    void aFailedTaskGivesTheFallback() throws InterruptedException {
        executor = Executors.newSingleThreadExecutor();
        List<Callable<Integer>> tasks = List.of(() -> 1, () -> {
            throw new IllegalStateException("broken");
        }, () -> 9);
        assertThat(Gather.all(executor, tasks, 5, TimeUnit.SECONDS, -1)).containsExactly(1, -1, 9);
    }

    @Test
    void anUnfinishedTaskIsCancelledAndGivesTheFallback() throws Exception {
        executor = Executors.newFixedThreadPool(2);
        CountDownLatch never = new CountDownLatch(1);
        List<Callable<Integer>> tasks = List.of(() -> 1, () -> {
            never.await(8, TimeUnit.SECONDS);
            return 2;
        });
        assertThat(Gather.all(executor, tasks, 300, TimeUnit.MILLISECONDS, -1)).containsExactly(1, -1);
        CountDownLatch both = new CountDownLatch(2);
        Callable<Boolean> meet = () -> {
            both.countDown();
            return both.await(5, TimeUnit.SECONDS);
        };
        java.util.concurrent.Future<Boolean> one = executor.submit(meet);
        java.util.concurrent.Future<Boolean> two = executor.submit(meet);
        assertThat(one.get(6, TimeUnit.SECONDS) && two.get(6, TimeUnit.SECONDS))
                .as("both pool threads are free once the unfinished task is cancelled").isTrue();
    }
}
