package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class IsolatedPoolTest {

    /** A task that remembers the pool it ran in, then returns its value or throws its failure. */
    private static final class Probe extends RecursiveTask<Integer> {
        final AtomicReference<ForkJoinPool> seen = new AtomicReference<>();
        private final int value;
        private final RuntimeException failure;

        Probe(int value, RuntimeException failure) {
            this.value = value;
            this.failure = failure;
        }

        @Override
        protected Integer compute() {
            seen.set(getPool());
            if (failure != null) {
                throw failure;
            }
            return value;
        }
    }

    @Test
    void returnsTheTasksResult() {
        assertThat(IsolatedPool.run(new Probe(42, null), 2)).isEqualTo(42);
    }

    @Test
    void runsInAFreshPoolOfThatParallelism() {
        for (int parallelism : new int[]{2, 3}) {
            Probe task = new Probe(42, null);
            IsolatedPool.run(task, parallelism);
            assertThat(task.seen.get()).as("the pool the task ran in").isNotNull();
            assertThat(task.seen.get()).isNotSameAs(ForkJoinPool.commonPool());
            assertThat(task.seen.get().getParallelism()).isEqualTo(parallelism);
        }
    }

    @Test
    void shutsThePoolDownAfterward() {
        Probe task = new Probe(7, null);
        assertThat(IsolatedPool.run(task, 3)).isEqualTo(7);
        assertThat(task.seen.get()).isNotNull();
        assertThat(task.seen.get().isShutdown()).as("the pool is shut down").isTrue();
    }

    @Test
    void shutsThePoolDownWhenTheTaskFails() {
        Probe task = new Probe(0, new IllegalStateException("boom"));
        Throwable thrown = catchThrowable(() -> IsolatedPool.run(task, 2));
        assertThat(thrown).as("the task's failure reaches the caller").isNotNull();
        assertThat(task.seen.get()).isNotNull();
        assertThat(task.seen.get().isShutdown()).as("the pool is shut down after a failure").isTrue();
    }

    @Test
    void aFailureReachesTheCallerUnwrapped() {
        Throwable thrown = catchThrowable(() -> IsolatedPool.run(new Probe(0, new IllegalStateException("boom")), 2));
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shutsThePoolDownWhenTheTaskErrs() {
        AtomicReference<ForkJoinPool> seen = new AtomicReference<>();
        RecursiveTask<Integer> task = new RecursiveTask<>() {
            @Override
            protected Integer compute() {
                seen.set(getPool());
                throw new AssertionError("invariant broken");
            }
        };
        Throwable thrown = catchThrowable(() -> IsolatedPool.run(task, 2));
        assertThat(thrown).as("an Error reaches the caller").isInstanceOf(AssertionError.class);
        assertThat(seen.get()).isNotNull();
        assertThat(seen.get().isShutdown()).as("the pool is shut down after an Error too").isTrue();
    }
}
