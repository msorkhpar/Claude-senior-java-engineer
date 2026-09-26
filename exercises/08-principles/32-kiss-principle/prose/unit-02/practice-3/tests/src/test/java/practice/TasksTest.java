package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

class TasksTest {

    @Test
    void returnsEachResultInTaskOrder() throws Exception {
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            int n = i;
            tasks.add(() -> "result" + n);
        }
        assertThat(Tasks.runAll(tasks)).containsExactly("result1", "result2", "result3");
        assertThat(Tasks.runAll(List.<Callable<String>>of())).isEmpty();
    }

    @Test
    void theTasksRunAtTheSameTime() throws Exception {
        int n = 4;
        CountDownLatch allStarted = new CountDownLatch(n);
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            int id = i;
            tasks.add(() -> {
                // No task can finish until every task has started: they must all run at once.
                allStarted.countDown();
                if (!allStarted.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("task " + id + " never saw every task running");
                }
                return "task" + id;
            });
        }
        assertThat(Tasks.runAll(tasks)).containsExactly("task1", "task2", "task3", "task4");
    }

    @Test
    void eachTaskRunsOnAVirtualThread() throws Exception {
        List<Callable<Boolean>> tasks = List.of(
                () -> Thread.currentThread().isVirtual(),
                () -> Thread.currentThread().isVirtual());
        assertThat(Tasks.runAll(tasks)).containsExactly(true, true);
    }

    @Test
    void aFailingTaskFailsTheCall() throws Exception {
        IllegalStateException boom = new IllegalStateException("boom");
        List<Callable<String>> tasks = List.of(
                () -> "fine",
                () -> {
                    throw boom;
                });
        Throwable thrown = catchThrowable(() -> Tasks.runAll(tasks));
        assertThat(thrown).isInstanceOf(ExecutionException.class);
        assertThat(thrown.getCause()).as("the task's own exception is the cause").isSameAs(boom);
    }
}
