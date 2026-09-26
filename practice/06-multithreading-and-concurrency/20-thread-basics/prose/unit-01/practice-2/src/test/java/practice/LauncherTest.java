package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LauncherTest {

    @Test
    void startsANamedThreadThatRunsTheTask() throws InterruptedException {
        AtomicInteger runs = new AtomicInteger();
        Thread io = Launcher.start(Launcher.Work.IO_BOUND, "handler-1", runs::incrementAndGet);
        Thread cpu = Launcher.start(Launcher.Work.CPU_BOUND, "cruncher-1", runs::incrementAndGet);
        io.join(2_000);
        cpu.join(2_000);
        assertThat(runs.get()).isEqualTo(2);
        assertThat(io.getName()).isEqualTo("handler-1");
        assertThat(cpu.getName()).isEqualTo("cruncher-1");
    }

    @Test
    void ioBoundWorkRunsOnAVirtualThread() throws InterruptedException {
        Thread io = Launcher.start(Launcher.Work.IO_BOUND, "handler-2", () -> { });
        io.join(2_000);
        assertThat(io.isVirtual()).isTrue();
    }

    @Test
    void cpuBoundWorkRunsOnAPlatformThread() throws InterruptedException {
        Thread cpu = Launcher.start(Launcher.Work.CPU_BOUND, "cruncher-2", () -> { });
        cpu.join(2_000);
        assertThat(cpu.isVirtual()).isFalse();
    }
}
