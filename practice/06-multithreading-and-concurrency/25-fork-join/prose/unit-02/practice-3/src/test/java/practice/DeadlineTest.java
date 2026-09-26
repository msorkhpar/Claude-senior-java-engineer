package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

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
}
