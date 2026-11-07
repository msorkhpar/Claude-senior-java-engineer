package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScoringClientTest {

    private final ExecutorService callers = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    /** A vendor whose calls stay open until the test lets them finish. */
    static final class HeldVendor implements ScoringClient.Vendor {
        final AtomicInteger inside = new AtomicInteger();
        final AtomicInteger peak = new AtomicInteger();
        final CountDownLatch entered;
        final CountDownLatch finish = new CountDownLatch(1);

        HeldVendor(int expectedEntries) {
            entered = new CountDownLatch(expectedEntries);
        }

        @Override
        public int score(String payment) throws Exception {
            peak.accumulateAndGet(inside.incrementAndGet(), Math::max);
            entered.countDown();
            try {
                if (!finish.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("test never finished the call");
                return payment.length() * 7;
            } finally {
                inside.decrementAndGet();
            }
        }
    }

    @AfterEach
    void cleanUp() {
        callers.shutdownNow();
        Thread.interrupted();
    }

    @Test
    void callsBeyondTheLimitWaitAndAllGetScored() throws Exception {
        HeldVendor vendor = new HeldVendor(4);
        ScoringClient client = new ScoringClient(vendor, 4, 10_000);
        List<Future<Integer>> scores = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            String payment = "PAY-58" + i;
            scores.add(callers.submit(() -> client.score(payment)));
        }

        assertThat(vendor.entered.await(5, TimeUnit.SECONDS)).isTrue();
        vendor.finish.countDown();

        for (Future<Integer> s : scores) assertThat(s.get(5, TimeUnit.SECONDS)).isEqualTo(49);
        assertThat(vendor.peak.get()).isEqualTo(4);
        assertThat(client.freeSessions()).isEqualTo(4);
    }

    @Test
    void aVendorFailureGivesItsSessionBack() throws Exception {
        Set<String> failing = Set.of("PAY-9901", "PAY-9902");
        ScoringClient client = new ScoringClient(payment -> {
            if (failing.contains(payment)) throw new IOException("vendor reset the connection");
            return 13;
        }, 2, 200);

        assertThatThrownBy(() -> client.score("PAY-9901")).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> client.score("PAY-9902")).isInstanceOf(IOException.class);

        assertThat(client.freeSessions()).isEqualTo(2);
        assertThat(client.score("PAY-9903")).isEqualTo(13);
    }

    @Test
    void aRefusedCallLeavesTheLimitAsItWas() throws Exception {
        HeldVendor vendor = new HeldVendor(1);
        ScoringClient client = new ScoringClient(vendor, 1, 50);
        Future<Integer> holder = callers.submit(() -> client.score("PAY-4410"));
        assertThat(vendor.entered.await(5, TimeUnit.SECONDS)).isTrue();

        assertThatThrownBy(() -> client.score("PAY-4411")).isInstanceOf(ScoringClient.Busy.class);

        assertThat(client.freeSessions()).isEqualTo(0);
        vendor.finish.countDown();
        holder.get(5, TimeUnit.SECONDS);
        assertThat(client.freeSessions()).isEqualTo(1);
    }

    @Test
    void aCallCancelledWhileWaitingLeavesTheLimitAsItWas() throws Exception {
        HeldVendor vendor = new HeldVendor(1);
        ScoringClient client = new ScoringClient(vendor, 1, 10_000);
        Future<Integer> holder = callers.submit(() -> client.score("PAY-6620"));
        assertThat(vendor.entered.await(5, TimeUnit.SECONDS)).isTrue();

        CountDownLatch ended = new CountDownLatch(1);
        Future<Integer> waiter = callers.submit(() -> {
            try {
                return client.score("PAY-6621");
            } finally {
                ended.countDown();
            }
        });
        long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (client.waitingCallers() == 0 && System.nanoTime() < until) Thread.onSpinWait();
        assertThat(client.waitingCallers()).isEqualTo(1);

        waiter.cancel(true);
        assertThat(ended.await(5, TimeUnit.SECONDS)).isTrue();

        assertThat(client.freeSessions()).isEqualTo(0);
        vendor.finish.countDown();
        holder.get(5, TimeUnit.SECONDS);
        assertThat(client.freeSessions()).isEqualTo(1);
    }
}
