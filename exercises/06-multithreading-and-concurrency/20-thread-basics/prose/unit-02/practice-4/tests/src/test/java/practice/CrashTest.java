package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class CrashTest {

    @Test
    void reportsTheExceptionOrNothing() throws InterruptedException {
        assertThat(Crash.runAndReport("importer", () -> {
            throw new RuntimeException("disk full");
        })).isEqualTo("importer: disk full");
        assertThat(Crash.runAndReport("quiet", () -> { })).isNull();
    }

    @Test
    void anErrorIsReportedToo() throws InterruptedException {
        assertThat(Crash.runAndReport("checker", () -> {
            throw new AssertionError("invariant broken");
        })).isEqualTo("checker: invariant broken");
    }

    @Test
    void theTaskRunsOnItsOwnThread() throws InterruptedException {
        AtomicReference<Thread> ranOn = new AtomicReference<>();
        String report = Crash.runAndReport("loader", () -> ranOn.set(Thread.currentThread()));
        assertThat(report).isNull();
        assertThat(ranOn.get()).isNotNull().isNotSameAs(Thread.currentThread());
        assertThat(ranOn.get().getName()).isEqualTo("loader");
    }
}
