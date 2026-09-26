package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class AuditLogTest {

    private static final Runnable NOTHING = () -> { };

    private static Thread start(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    @Test
    void recordsABatchInOrder() {
        AuditLog log = new AuditLog();
        log.recordAll(List.of("a", "b"), NOTHING);
        log.record("c");
        assertThat(log.entries()).isEqualTo("a,b,c");
    }

    /**
     * After the batch's first entry, another thread records "x" and the hook waits (at most 3 s)
     * until that thread is parked on the lock or has finished.
     */
    @Test
    void batchIsNotInterleaved() throws InterruptedException {
        AuditLog log = new AuditLog();
        AtomicReference<Thread> other = new AtomicReference<>();
        AtomicReference<Thread.State> seen = new AtomicReference<>();
        Runnable afterEach = () -> {
            if (other.get() != null) {
                return;
            }
            Thread t = start(() -> log.record("x"));
            other.set(t);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (System.nanoTime() < deadline) {
                Thread.State s = t.getState();
                if (s == Thread.State.WAITING || s == Thread.State.TERMINATED) {
                    break;
                }
                Thread.onSpinWait();
            }
            seen.set(t.getState());
        };
        log.recordAll(List.of("a1", "a2"), afterEach);
        other.get().join(3_000);
        assertThat(seen.get()).as("the other thread waited for the batch").isEqualTo(Thread.State.WAITING);
        assertThat(log.entries()).isEqualTo("a1,a2,x");
    }

    @Test
    void lockIsFreeAfterBatch() throws InterruptedException {
        AuditLog log = new AuditLog();
        log.recordAll(List.of("a", "b"), NOTHING);
        Thread other = start(() -> log.record("c"));
        other.join(3_000);
        boolean finished = !other.isAlive();
        assertThat(finished).as("the batch left the lock fully released").isTrue();
        assertThat(log.entries()).isEqualTo("a,b,c");
    }
}
