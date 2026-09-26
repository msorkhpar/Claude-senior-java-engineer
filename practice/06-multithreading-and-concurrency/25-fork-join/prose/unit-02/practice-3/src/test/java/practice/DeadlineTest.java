package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class DeadlineTest {

    /** Waits on a latch nobody opens; counts down `interrupted` when its wait is interrupted. */
    private static final class Stuck extends RecursiveTask<Integer> {
        final CountDownLatch never = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);

        @Override
        protected Integer compute() {
            try {
                never.await();
            } catch (InterruptedException e) {
                interrupted.countDown();
            }
            return -1;
        }
    }

    private static RecursiveTask<Integer> returning(int value) {
        return new RecursiveTask<>() {
            @Override
            protected Integer compute() {
                return value;
            }
        };
    }

    @Test
    void returnsAResultInTime() {
        assertThat(Deadline.within(returning(42), 2, 5_000)).contains(42);
    }

    @Test
    void aTaskThatMissesTheDeadlineGivesEmpty() {
        Stuck stuck = new Stuck();
        Optional<Integer> result = Deadline.within(stuck, 1, 200);
        stuck.never.countDown();
        assertThat(result).isEmpty();
    }

    @Test
    void aTimedOutTaskIsInterrupted() throws InterruptedException {
        Stuck stuck = new Stuck();
        Deadline.within(stuck, 1, 200);
        boolean interrupted = stuck.interrupted.await(5, TimeUnit.SECONDS);
        stuck.never.countDown();
        assertThat(interrupted).as("the stuck task's wait was interrupted").isTrue();
    }

    @Test
    void aFailureIsReportedWithItsCause() {
        RecursiveTask<Integer> failing = new RecursiveTask<>() {
            @Override
            protected Integer compute() {
                throw new IllegalArgumentException("bad input");
            }
        };
        Throwable thrown = catchThrowable(() -> Deadline.within(failing, 2, 5_000));
        assertThat(thrown).isInstanceOf(IllegalStateException.class).hasRootCauseMessage("bad input");
        assertThat(thrown.getCause()).as("the cause is the task's own exception, not the ExecutionException")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void thePoolIsShutDownAfterSuccess() {
        AtomicReference<ForkJoinPool> seen = new AtomicReference<>();
        RecursiveTask<Integer> task = new RecursiveTask<>() {
            @Override
            protected Integer compute() {
                seen.set(getPool());
                return 5;
            }
        };
        assertThat(Deadline.within(task, 2, 5_000)).contains(5);
        assertThat(seen.get()).isNotNull();
        assertThat(seen.get().isShutdown()).as("the pool is shut down after a result in time too").isTrue();
    }

    @Test
    void anInterruptedCallerKeepsItsFlag() {
        Stuck task = new Stuck();
        Thread.currentThread().interrupt();
        Optional<Integer> result;
        boolean flagAfter;
        try {
            result = Deadline.within(task, 2, 5_000);
        } finally {
            flagAfter = Thread.interrupted();
        }
        assertThat(result).isEmpty();
        assertThat(flagAfter).as("the caller's interrupt flag is set again, not swallowed").isTrue();
    }

    @Test
    void theTimeoutIsInMilliseconds() {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch open = new CountDownLatch(1);
        RecursiveTask<Integer> slow = new RecursiveTask<>() {
            @Override
            protected Integer compute() {
                started.countDown();
                try {
                    open.await();
                } catch (InterruptedException e) {
                    return -1;
                }
                return 9;
            }
        };
        Thread opener = new Thread(() -> {
            try {
                started.await();
                Thread.sleep(300);
            } catch (InterruptedException e) {
                return;
            }
            open.countDown();
        });
        opener.setDaemon(true);
        opener.start();
        assertThat(Deadline.within(slow, 2, 5_000)).as("a task done after 300 ms is in time for 5000 ms").contains(9);
    }
}
