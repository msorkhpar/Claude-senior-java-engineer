package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class GatherTest {

    /**
     * Tasks that finish in a forced order: task {@code order[k]} does not return until the thread of
     * task {@code order[k - 1]} has ended, which is after that task's result was recorded.
     */
    private static List<Callable<String>> finishingIn(int... order) {
        int n = order.length;
        Thread[] threads = new Thread[n];
        CountDownLatch[] started = new CountDownLatch[n];
        for (int i = 0; i < n; i++) {
            started[i] = new CountDownLatch(1);
        }
        int[] before = new int[n];
        for (int k = 0; k < n; k++) {
            before[order[k]] = k == 0 ? -1 : order[k - 1];
        }
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int me = i;
            tasks.add(() -> {
                threads[me] = Thread.currentThread();
                started[me].countDown();
                int prev = before[me];
                if (prev >= 0) {
                    started[prev].await();
                    threads[prev].join();
                }
                return "task-" + me;
            });
        }
        return tasks;
    }

    @Test
    void gathersEveryResult() {
        List<Gather.Outcome<String>> outcomes = Gather.all(finishingIn(0, 1, 2));
        assertThat(outcomes).containsExactly(
                new Gather.Outcome<>("task-0", null),
                new Gather.Outcome<>("task-1", null),
                new Gather.Outcome<>("task-2", null));
        assertThat(Gather.all(List.<Callable<String>>of())).isEmpty();
    }

    @Test
    void outcomesFollowTaskOrder() {
        List<Gather.Outcome<String>> outcomes = Gather.all(finishingIn(2, 1, 0));
        assertThat(outcomes).extracting(Gather.Outcome::value).containsExactly("task-0", "task-1", "task-2");
    }

    @Test
    void tasksRunOnVirtualThreads() {
        List<Callable<Boolean>> tasks = List.of(
                () -> Thread.currentThread().isVirtual(),
                () -> Thread.currentThread().isVirtual());
        assertThat(Gather.all(tasks)).extracting(Gather.Outcome::value).containsExactly(true, true);
    }

    @Test
    void failuresAreReportedInPlace() {
        List<Callable<Integer>> tasks = List.of(
                () -> 1000,
                () -> {
                    throw new IllegalStateException("boom");
                },
                () -> 3000);
        assertThat(Gather.all(tasks)).containsExactly(
                new Gather.Outcome<>(1000, null),
                new Gather.Outcome<>(null, "boom"),
                new Gather.Outcome<>(3000, null));
    }
}
