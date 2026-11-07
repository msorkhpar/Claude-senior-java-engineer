package practice;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class SessionSweeperTest {

    private static <T> T within(Callable<T> call) throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            return pool.submit(call).get(10, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }

    /** Whether waiter is blocked on a monitor that holder owns. */
    private static boolean blockedBy(Thread waiter, Thread holder) {
        ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(waiter.threadId());
        return info != null && info.getThreadState() == Thread.State.BLOCKED
                && info.getLockOwnerId() == holder.threadId();
    }

    private static SessionSweeper sessions() {
        SessionSweeper s = new SessionSweeper();
        s.touch("sid-a1", 1_200L);
        s.touch("sid-b2", 400L);
        s.touch("sid-c3", 1_900L);
        s.touch("sid-d4", 650L);
        s.touch("sid-e5", 2_300L);
        return s;
    }

    @Test
    void sweepsTheExpiredAndKeepsTheRest() throws Exception {
        SessionSweeper s = sessions();

        List<String> gone = within(() -> s.sweep((id, seen) -> seen < 1_000L));

        assertThat(gone).containsExactlyInAnyOrder("sid-b2", "sid-d4");
        assertThat(s.lastSeen("sid-b2")).isNull();
        assertThat(s.lastSeen("sid-d4")).isNull();
        assertThat(within(s::snapshot)).isEqualTo(Map.of("sid-a1", 1_200L, "sid-c3", 1_900L, "sid-e5", 2_300L));
    }

    /**
     * When the policy is asked about the first session, a request thread touches a new session.
     * The policy then waits (at most 3 s) until that thread is either blocked on a lock the
     * sweeping thread holds, or has finished its touch.
     */
    @Test
    void aTouchDuringTheSweepWaitsForTheSweep() throws Exception {
        SessionSweeper s = sessions();
        AtomicReference<Thread> request = new AtomicReference<>();
        AtomicBoolean waited = new AtomicBoolean();

        List<String> gone = within(() -> s.sweep((id, seen) -> {
            if (request.get() == null) {
                Thread r = new Thread(() -> s.touch("sid-new", 3_000L));
                r.setDaemon(true);
                request.set(r);
                r.start();
                Thread me = Thread.currentThread();
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
                while (System.nanoTime() < deadline && r.isAlive()) {
                    if (blockedBy(r, me)) {
                        waited.set(true);
                        break;
                    }
                    Thread.onSpinWait();
                }
            }
            return seen < 1_000L;
        }));

        request.get().join(5_000);
        assertThat(waited.get()).as("the touch waited until the sweep was over").isTrue();
        assertThat(request.get().isAlive()).as("the touch finished after the sweep").isFalse();
        assertThat(gone).containsExactlyInAnyOrder("sid-b2", "sid-d4");
        assertThat(s.lastSeen("sid-new")).isEqualTo(3_000L);
    }

    @Test
    void aSnapshotStaysAsItWasWhenTaken() throws Exception {
        SessionSweeper s = new SessionSweeper();
        s.touch("sid-p7", 10L);
        s.touch("sid-q8", 20L);

        Map<String, Long> snap = within(s::snapshot);
        s.touch("sid-q8", 99L);
        s.touch("sid-r9", 30L);

        assertThat(snap).isEqualTo(Map.of("sid-p7", 10L, "sid-q8", 20L));
    }
}
