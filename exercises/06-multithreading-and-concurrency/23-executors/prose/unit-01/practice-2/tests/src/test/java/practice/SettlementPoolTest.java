package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class SettlementPoolTest {

    private final List<Throwable> alerts = Collections.synchronizedList(new ArrayList<>());
    private final SettlementPool pool = new SettlementPool(1, alerts::add);

    @AfterEach
    void stopThePool() {
        pool.shutdownNow();
    }

    /** Lets every job handed over so far finish, and the hooks after them run. */
    private void finish() throws InterruptedException {
        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).as("the pool finished its jobs").isTrue();
    }

    @Test
    void jobsThatSucceedAllRunAndRaiseNoAlert() throws Exception {
        AtomicInteger settled = new AtomicInteger();
        List<String> workers = Collections.synchronizedList(new ArrayList<>());
        Runnable settle = () -> {
            workers.add(Thread.currentThread().getName());
            settled.incrementAndGet();
        };
        pool.execute(settle);
        pool.submit(settle);
        pool.submit(() -> { settle.run(); return "batch 31 settled"; });
        pool.execute(settle);

        finish();

        assertThat(settled).hasValue(4);
        assertThat(alerts).isEmpty();
        assertThat(workers).as("every job ran on the pool's one worker").containsOnly(workers.get(0));
    }

    @Test
    void aSubmittedJobThatFailsRaisesAnAlert() throws Exception {
        pool.submit(() -> { throw new IOException("ledger-7 unreachable"); });
        pool.submit(() -> "batch 32 settled");

        finish();

        assertThat(alerts).hasSize(1);
    }

    @Test
    void aJobHandedOverWithExecuteThatFailsRaisesAnAlert() throws Exception {
        IllegalStateException declined = new IllegalStateException("payee account closed");
        pool.execute(() -> { throw declined; });

        finish();

        assertThat(alerts).containsExactly(declined);
    }

    @Test
    void theAlertCarriesTheExceptionTheJobThrew() throws Exception {
        IOException unreachable = new IOException("ledger-9 unreachable");
        pool.submit(() -> { throw unreachable; });

        finish();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0)).isSameAs(unreachable);
    }

    @Test
    void aJobCancelledWhileItRanIsNotAFailure() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch goOn = new CountDownLatch(1);
        List<String> threads = Collections.synchronizedList(new ArrayList<>());
        Future<?> rerun = pool.submit(() -> {
            threads.add(Thread.currentThread().getName());
            started.countDown();
            goOn.await(5, TimeUnit.SECONDS);
            return "batch 33 settled";
        });
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
        rerun.cancel(false);                                     // an operator cancels the rerun
        goOn.countDown();
        pool.submit(() -> threads.add(Thread.currentThread().getName()));

        finish();

        assertThat(alerts).as("alerts").isEmpty();
        assertThat(threads).as("the next job ran on the same worker").hasSize(2).containsOnly(threads.get(0));
    }
}
