package practice;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class MaintenanceScheduleTest {

    private static final MaintenanceSchedule.Window NONE = MaintenanceSchedule.Window.NONE;

    /** A call into the schedule, made on its own daemon thread so that a call that blocks can't hang the run. */
    private static final class Call<T> {
        final FutureTask<T> task;
        final Thread thread;

        Call(String name, Callable<T> body) {
            task = new FutureTask<>(body);
            thread = new Thread(task, name);
            thread.setDaemon(true);
            thread.start();
        }

        T result() throws Exception {
            return task.get(10, TimeUnit.SECONDS);
        }
    }

    private static <T> T call(Callable<T> body) throws Exception {
        return new Call<>("caller", body).result();
    }

    /**
     * Returns once {@code call} has finished, or is blocked on a monitor that {@code owner} holds,
     * whichever comes first (or after five seconds).
     */
    private static void awaitDoneOrBlockedBy(Call<?> call, Thread owner) {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!call.task.isDone() && System.nanoTime() < deadline) {
            ThreadInfo info = mx.getThreadInfo(call.thread.threadId());
            if (info != null && info.getThreadState() == Thread.State.BLOCKED
                    && info.getLockOwnerId() == owner.threadId()) {
                return;
            }
            Thread.onSpinWait();
        }
    }

    /** A schedule whose reschedule() stops halfway, after its first write, until released. */
    private static final class Paused {
        final CountDownLatch halfway = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final MaintenanceSchedule schedule = new MaintenanceSchedule(() -> {
            halfway.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @Test
    void returnsTheWindowLastScheduled() throws Exception {
        MaintenanceSchedule schedule = new MaintenanceSchedule();
        call(() -> { schedule.reschedule(1_290, 1_335); return null; });       // 21:30 to 22:15

        assertThat(call(schedule::current)).isEqualTo(new MaintenanceSchedule.Window(1_290, 1_335));
    }

    @Test
    void cancelRemovesTheWindowUntilTheNextReschedule() throws Exception {
        MaintenanceSchedule schedule = new MaintenanceSchedule();
        call(() -> { schedule.cancel(); return null; });
        assertThat(call(schedule::current)).isEqualTo(NONE);

        call(() -> { schedule.reschedule(45, 75); return null; });
        assertThat(call(schedule::current)).isEqualTo(new MaintenanceSchedule.Window(45, 75));
    }

    @Test
    void aPageViewDuringARescheduleShowsAWindowThatWasScheduled() throws Exception {
        Paused p = new Paused();
        Call<Void> operator = new Call<>("operator", () -> { p.schedule.reschedule(600, 660); return null; });
        try {
            assertThat(p.halfway.await(5, TimeUnit.SECONDS)).isTrue();
            Call<MaintenanceSchedule.Window> view = new Call<>("page-view", p.schedule::current);
            awaitDoneOrBlockedBy(view, operator.thread);
            p.release.countDown();

            assertThat(view.result()).isIn(new MaintenanceSchedule.Window(120, 180),
                    new MaintenanceSchedule.Window(600, 660));
        } finally {
            p.release.countDown();
        }
        operator.result();
    }

    @Test
    void aCancelDuringARescheduleLeavesNoHalfWindow() throws Exception {
        Paused p = new Paused();
        Call<Void> operator = new Call<>("operator", () -> { p.schedule.reschedule(600, 660); return null; });
        try {
            assertThat(p.halfway.await(5, TimeUnit.SECONDS)).isTrue();
            Call<Void> onCall = new Call<>("on-call", () -> { p.schedule.cancel(); return null; });
            awaitDoneOrBlockedBy(onCall, operator.thread);
            p.release.countDown();
            operator.result();
            onCall.result();
        } finally {
            p.release.countDown();
        }

        assertThat(call(p.schedule::current)).isEqualTo(NONE);
    }
}
