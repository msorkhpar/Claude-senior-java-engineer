package practice;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TaxTablesTest {

    /** A call into the tables, made on its own daemon thread so that a call that blocks can't hang the run. */
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

    /**
     * Returns once {@code call} has finished, or is blocked on a monitor that {@code owner} holds,
     * whichever comes first (or after five seconds).
     */
    private static void awaitDoneOrBlockedBy(Call<?> call, Thread owner) {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!call.task.isDone() && System.nanoTime() < deadline) {
            if (blockedBy(mx, call.thread, owner)) return;
            Thread.onSpinWait();
        }
    }

    private static boolean blockedBy(ThreadMXBean mx, Thread t, Thread owner) {
        ThreadInfo info = mx.getThreadInfo(t.threadId());
        return info != null && info.getThreadState() == Thread.State.BLOCKED
                && info.getLockOwnerId() == owner.threadId();
    }

    /** A tax service that counts its calls and can hold the first one until released. */
    private static final class TaxService implements TaxTables.Loader {
        final AtomicInteger calls = new AtomicInteger();
        final CountDownLatch firstCallStarted = new CountDownLatch(1);
        final CountDownLatch finishFirstCall;

        TaxService(boolean holdFirstCall) {
            finishFirstCall = new CountDownLatch(holdFirstCall ? 1 : 0);
        }

        @Override
        public TaxTables.Rates load() {
            int n = calls.incrementAndGet();
            if (n == 1) {
                firstCallStarted.countDown();
                try {
                    finishFirstCall.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return new TaxTables.Rates("2025-Q" + n, Map.of("DE", 1_900, "FR", 2_000, "LU", 1_700));
        }
    }

    @Test
    void fetchesOnFirstUseAndKeepsWhatItFetched() throws Exception {
        TaxService service = new TaxService(false);
        TaxTables tables = new TaxTables(service);
        assertThat(service.calls).hasValue(0);

        TaxTables.Rates first = new Call<>("checkout-1", tables::rates).result();
        TaxTables.Rates second = new Call<>("checkout-2", tables::rates).result();

        assertThat(first.basisPoints("LU")).isEqualTo(1_700);
        assertThat(second).isSameAs(first);
        assertThat(service.calls).hasValue(1);
    }

    @Test
    void twoFirstCallsAtOnceFetchTheTablesOnce() throws Exception {
        TaxService service = new TaxService(true);
        TaxTables tables = new TaxTables(service);
        Call<TaxTables.Rates> a = new Call<>("checkout-a", tables::rates);
        try {
            assertThat(service.firstCallStarted.await(5, TimeUnit.SECONDS)).isTrue();
            Call<TaxTables.Rates> b = new Call<>("checkout-b", tables::rates);
            awaitDoneOrBlockedBy(b, a.thread);
            service.finishFirstCall.countDown();

            assertThat(b.result()).isSameAs(a.result());
            assertThat(service.calls).hasValue(1);
        } finally {
            service.finishFirstCall.countDown();
        }
    }

    @Test
    void onceFetchedACheckoutDoesNotQueueBehindTheLock() throws Exception {
        TaxTables tables = new TaxTables(new TaxService(false));
        TaxTables.Rates fetched = new Call<>("checkout-1", tables::rates).result();

        Field lockField = TaxTables.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        Object lock = lockField.get(tables);
        CountDownLatch holding = new CountDownLatch(1);
        CountDownLatch letGo = new CountDownLatch(1);
        Call<Void> holder = new Call<>("holder", () -> {          // holds the tables' lock and the tables themselves
            synchronized (tables) {
                synchronized (lock) {
                    holding.countDown();
                    letGo.await(10, TimeUnit.SECONDS);
                }
            }
            return null;
        });
        try {
            assertThat(holding.await(5, TimeUnit.SECONDS)).isTrue();
            Call<TaxTables.Rates> checkout = new Call<>("checkout-2", tables::rates);
            awaitDoneOrBlockedBy(checkout, holder.thread);

            assertThat(checkout.task.isDone()).as("rates() returned while another thread held the lock").isTrue();
            assertThat(checkout.result()).isSameAs(fetched);
        } finally {
            letGo.countDown();
        }
        holder.result();
    }

    @Test
    void theFieldReadWithoutTheLockPublishesTheTables() throws Exception {
        Field rates = TaxTables.class.getDeclaredField("rates");

        assertThat(Modifier.isVolatile(rates.getModifiers()))
                .as("rates() reads 'rates' without the lock; a thread can see the reference before the tables behind it")
                .isTrue();
    }
}
