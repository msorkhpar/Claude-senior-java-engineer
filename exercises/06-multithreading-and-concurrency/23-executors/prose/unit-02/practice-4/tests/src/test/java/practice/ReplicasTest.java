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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ReplicasTest {

    private ExecutorService executor;

    @AfterEach
    void stop() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void takesTheOnlyReplyAndKeepsArrivals() throws Exception {
        executor = Executors.newSingleThreadExecutor();
        List<Callable<String>> only = List.of(() -> "r1");
        assertThat(Replicas.first(executor, only)).isEqualTo("r1");
        List<Callable<String>> tasks = List.of(() -> "a", () -> "b");
        assertThat(Replicas.inArrivalOrder(executor, tasks)).containsExactly("a", "b");
    }

    @Test
    void firstSkipsAFailedReplica() throws Exception {
        executor = Executors.newFixedThreadPool(2);
        CountDownLatch failed = new CountDownLatch(1);
        List<Callable<String>> replicas = List.of(
                () -> {
                    failed.countDown();
                    throw new IllegalStateException("replica down");
                },
                () -> {
                    failed.await(5, TimeUnit.SECONDS);
                    return "ok";
                });
        assertThat(Replicas.first(executor, replicas)).isEqualTo("ok");
    }

    @Test
    void firstCancelsTheSlowReplicas() throws Exception {
        executor = Executors.newFixedThreadPool(2);
        CountDownLatch never = new CountDownLatch(1);
        CountDownLatch slowStarted = new CountDownLatch(1);
        List<Callable<String>> replicas = List.of(
                () -> {
                    slowStarted.countDown();
                    never.await(8, TimeUnit.SECONDS);
                    return "slow";
                },
                () -> {
                    slowStarted.await(5, TimeUnit.SECONDS);
                    return "fast";
                });
        assertThat(Replicas.first(executor, replicas)).isEqualTo("fast");
        CountDownLatch both = new CountDownLatch(2);
        Callable<Boolean> meet = () -> {
            both.countDown();
            return both.await(5, TimeUnit.SECONDS);
        };
        Future<Boolean> one = executor.submit(meet);
        Future<Boolean> two = executor.submit(meet);
        assertThat(one.get(6, TimeUnit.SECONDS) && two.get(6, TimeUnit.SECONDS))
                .as("both pool threads are free once the slow replica is cancelled").isTrue();
    }

    @Test
    void firstFailsWhenEveryReplicaFails() {
        executor = Executors.newFixedThreadPool(2);
        List<Callable<String>> replicas = List.of(
                () -> {
                    throw new IllegalStateException("down 1");
                },
                () -> {
                    throw new IllegalStateException("down 2");
                });
        assertThatThrownBy(() -> Replicas.first(executor, replicas)).isInstanceOf(ExecutionException.class);
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
    void arrivalsFollowCompletionOrder() throws Exception {
        HeldExecutor held = new HeldExecutor();
        executor = held;
        List<Callable<String>> tasks = List.of(() -> "a", () -> "b", () -> "c");
        AtomicReference<List<String>> results = new AtomicReference<>();
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread caller = new Thread(() -> {
            try {
                results.set(Replicas.inArrivalOrder(held, tasks));
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
        assertThat(caller.isAlive()).as("inArrivalOrder returned").isFalse();
        assertThat(thrown.get()).isNull();
        assertThat(results.get()).containsExactly("c", "b", "a");
    }

    @Test
    void aFailedArrivalIsReported() throws Exception {
        executor = Executors.newFixedThreadPool(2);
        List<Callable<String>> tasks = List.of(() -> "a", () -> {
            throw new IllegalStateException("broken");
        });
        assertThatThrownBy(() -> Replicas.inArrivalOrder(executor, tasks))
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void aSlowArrivalIsWaitedFor() throws Exception {
        executor = Executors.newFixedThreadPool(2);
        List<Callable<String>> tasks = List.of(() -> {
            Thread.sleep(1_500);
            return "slow";
        }, () -> "quick");
        assertThat(Replicas.inArrivalOrder(executor, tasks)).as("a task that takes 1.5 s is still waited for").containsExactly("quick", "slow");
    }
}
