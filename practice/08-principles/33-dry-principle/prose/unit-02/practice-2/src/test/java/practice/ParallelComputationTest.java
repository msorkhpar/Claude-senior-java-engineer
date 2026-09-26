package practice;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

class ParallelComputationTest {

    @Test
    void computationsShareOneLifecycle() throws Exception {
        assertThat(ParallelComputation.squares(List.of(1, 2, 3, 200), 1)).containsExactly(1, 4, 9, 40000);
        assertThat(ParallelComputation.cubes(List.of(2, 3), 1)).containsExactly(8, 27);
        List<Callable<String>> tasks = List.of(() -> "a", () -> "b");
        assertThat(ParallelComputation.executeAll(tasks, 1)).containsExactly("a", "b");
        assertThat(ParallelComputation.executeAll(List.<Callable<String>>of(), 1)).isEmpty();
        assertThatThrownBy(() -> ParallelComputation.executeAll(tasks, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resultsFollowTaskOrderNotFinishingOrder() throws Exception {
        CountDownLatch secondStarted = new CountDownLatch(1);
        AtomicReference<Thread> secondWorker = new AtomicReference<>();
        Callable<String> first = () -> {
            secondStarted.await();
            // wait until the second task has finished and its worker is idle again
            while (secondWorker.get().getState() != Thread.State.WAITING) {
                Thread.onSpinWait();
            }
            return "first";
        };
        Callable<String> second = () -> {
            secondWorker.set(Thread.currentThread());
            secondStarted.countDown();
            return "second";
        };
        assertThat(ParallelComputation.executeAll(List.of(first, second), 2)).containsExactly("first", "second");
    }

    @Test
    void thePoolIsShutDownBeforeReturning() throws Exception {
        AtomicReference<Thread> worker = new AtomicReference<>();
        ParallelComputation.executeAll(List.<Callable<Integer>>of(() -> {
            worker.set(Thread.currentThread());
            return 1;
        }), 1);
        worker.get().join(10_000);
        assertThat(worker.get().isAlive()).as("the pool's thread after executeAll returned").isFalse();
    }

    @Test
    void aFailureKeepsTheTasksOwnException() throws Exception {
        IllegalStateException bug = new IllegalStateException("bug");
        java.util.List<java.util.concurrent.Callable<Integer>> failing = java.util.List.of(() -> {
            throw bug;
        });
        Throwable thrown = catchThrowable(() -> ParallelComputation.executeAll(failing, 1));
        assertThat(thrown).isInstanceOf(RuntimeException.class).isNotSameAs(bug)
                .hasCauseInstanceOf(java.util.concurrent.ExecutionException.class);
        assertThat(thrown.getCause().getCause()).isSameAs(bug);
    }

    @Test
    void theThreadCountIsCheckedFirst() throws Exception {
        assertThatThrownBy(() -> ParallelComputation.executeAll(java.util.List.<java.util.concurrent.Callable<Integer>>of(), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
