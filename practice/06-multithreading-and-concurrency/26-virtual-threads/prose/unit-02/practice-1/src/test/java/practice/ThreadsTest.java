package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ThreadsTest {

    private static final Runnable NOTHING = () -> { };

    @Test
    void aPlainSpecGetsAVirtualThread() {
        Thread t = Threads.create(new Threads.Spec("worker-1", true, Thread.NORM_PRIORITY), NOTHING);
        assertThat(t.isVirtual()).isTrue();
        assertThat(t.getName()).isEqualTo("worker-1");
        assertThat(t.isDaemon()).isTrue();
        assertThat(t.getPriority()).isEqualTo(Thread.NORM_PRIORITY);
        assertThat(t.getState()).as("unstarted").isEqualTo(Thread.State.NEW);
        assertThat(t.getThreadGroup().getName()).isEqualTo("VirtualThreads");
    }

    @Test
    void theThreadRunsTheTask() throws InterruptedException {
        AtomicBoolean ran = new AtomicBoolean();
        Thread t = Threads.create(new Threads.Spec("worker-2", true, Thread.NORM_PRIORITY), () -> ran.set(true));
        t.start();
        t.join(5_000);
        assertThat(ran).isTrue();
    }

    @Test
    void aNonDaemonSpecGetsAPlatformThread() {
        Thread t = Threads.create(new Threads.Spec("keeper", false, Thread.NORM_PRIORITY), NOTHING);
        assertThat(t.isVirtual()).as("virtual").isFalse();
        assertThat(t.isDaemon()).as("daemon").isFalse();
        assertThat(t.getName()).isEqualTo("keeper");
        assertThat(t.getState()).as("unstarted").isEqualTo(Thread.State.NEW);
    }

    @Test
    void aPrioritySpecGetsAPlatformThread() {
        Thread t = Threads.create(new Threads.Spec("urgent", true, Thread.MAX_PRIORITY), NOTHING);
        assertThat(t.isVirtual()).as("virtual").isFalse();
        assertThat(t.getPriority()).isEqualTo(Thread.MAX_PRIORITY);
        assertThat(t.isDaemon()).as("daemon").isTrue();
        assertThat(t.getName()).isEqualTo("urgent");
        Thread low = Threads.create(new Threads.Spec("background", true, Thread.MIN_PRIORITY), NOTHING);
        assertThat(low.isVirtual()).as("virtual at MIN_PRIORITY").isFalse();
        assertThat(low.getPriority()).isEqualTo(Thread.MIN_PRIORITY);
    }
}
