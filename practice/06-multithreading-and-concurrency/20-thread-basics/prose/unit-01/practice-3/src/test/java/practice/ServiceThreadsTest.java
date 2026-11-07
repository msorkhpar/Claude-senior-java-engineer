package practice;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceThreadsTest {

    private static Thread madeOnADaemonThread(ServiceThreads factory) throws InterruptedException {
        AtomicReference<Thread> made = new AtomicReference<>();
        Thread housekeeping = new Thread(() -> made.set(factory.newThread(() -> {})), "housekeeping");
        housekeeping.setDaemon(true);
        housekeeping.start();
        housekeeping.join(5_000);
        return made.get();
    }

    @Test
    void aPoolsThreadsCarryTheComponentNameAndItsDeclaredDaemonStatus() throws Exception {
        ExecutorService ledger = Executors.newFixedThreadPool(2, new ServiceThreads("ledger", false));
        ExecutorService sampler = Executors.newFixedThreadPool(1, new ServiceThreads("cpu-sampler", true));
        try {
            Future<Thread> ledgerThread = ledger.submit(Thread::currentThread);
            Future<Thread> samplerThread = sampler.submit(Thread::currentThread);

            assertThat(ledgerThread.get(5, TimeUnit.SECONDS).getName()).matches("ledger-\\d+");
            assertThat(ledgerThread.get(5, TimeUnit.SECONDS).isDaemon()).isFalse();
            assertThat(samplerThread.get(5, TimeUnit.SECONDS).getName()).matches("cpu-sampler-\\d+");
            assertThat(samplerThread.get(5, TimeUnit.SECONDS).isDaemon()).isTrue();
        } finally {
            ledger.shutdownNow();
            sampler.shutdownNow();
        }
        Thread fresh = new ServiceThreads("ledger", false).newThread(() -> {});
        assertThat(fresh.getState()).isEqualTo(Thread.State.NEW);
    }

    @Test
    void eachFactoryNumbersItsOwnThreadsFromOne() {
        ServiceThreads ledger = new ServiceThreads("ledger", false);
        ServiceThreads mailer = new ServiceThreads("mailer", false);

        Thread l1 = ledger.newThread(() -> {});
        Thread l2 = ledger.newThread(() -> {});
        Thread m1 = mailer.newThread(() -> {});

        assertThat(l1.getName()).isEqualTo("ledger-1");
        assertThat(l2.getName()).isEqualTo("ledger-2");
        assertThat(m1.getName()).isEqualTo("mailer-1");
    }

    @Test
    void aForegroundComponentCreatedFromADaemonThreadStillHoldsTheJvmOpen() throws InterruptedException {
        Thread made = madeOnADaemonThread(new ServiceThreads("audit", false));

        assertThat(made).isNotNull();
        assertThat(made.isDaemon()).isFalse();
    }
}
