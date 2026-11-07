package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static practice.RequestPool.REQUEST_ID;

class RequestPoolTest {

    private RequestPool pool;

    @AfterEach
    void stopThePool() {
        if (pool != null) pool.shutdownNow();
        REQUEST_ID.remove();
    }

    @Test
    void aSubmittedCallableLogsUnderTheRequestThatSubmittedIt() throws Exception {
        pool = new RequestPool(2, 8);

        REQUEST_ID.set("ord-4411");
        Future<String> first = pool.submit(() -> REQUEST_ID.get());
        REQUEST_ID.set("ord-4412");
        Future<String> second = pool.submit(() -> REQUEST_ID.get());
        REQUEST_ID.remove();
        Future<String> third = pool.submit(() -> String.valueOf(REQUEST_ID.get()));

        assertThat(first.get(5, SECONDS)).isEqualTo("ord-4411");
        assertThat(second.get(5, SECONDS)).isEqualTo("ord-4412");
        assertThat(third.get(5, SECONDS)).isEqualTo("null");
    }

    @Test
    void aSubmittedRunnableLogsUnderTheRequestThatSubmittedIt() throws Exception {
        pool = new RequestPool(2, 8);
        AtomicReference<String> seen = new AtomicReference<>("not run");

        REQUEST_ID.set("ord-5120");
        Future<?> done = pool.submit(() -> { seen.set(REQUEST_ID.get()); });
        done.get(5, SECONDS);

        assertThat(seen.get()).isEqualTo("ord-5120");
    }

    @Test
    void housekeepingAfterARequestTaskLogsUnderNoRequest() throws Exception {
        pool = new RequestPool(1, 8);

        REQUEST_ID.set("ord-6001");
        assertThat(pool.submit(() -> REQUEST_ID.get()).get(5, SECONDS)).isEqualTo("ord-6001");
        REQUEST_ID.remove();

        CompletableFuture<String> seen = new CompletableFuture<>();
        pool.execute(() -> seen.complete(String.valueOf(REQUEST_ID.get())));
        assertThat(seen.get(5, SECONDS)).isEqualTo("null");
    }

    @Test
    void aHandlerThatRunsTheTaskItselfKeepsItsOwnRequestId() throws Exception {
        pool = new RequestPool(1, 1);
        CountDownLatch busy = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Thread> ranOn = new AtomicReference<>();

        REQUEST_ID.set("ord-7300");
        Future<String> overflow;
        try {
            pool.submit(() -> {
                busy.countDown();
                release.await(5, TimeUnit.SECONDS);
                return "slow";
            });
            assertThat(busy.await(5, SECONDS)).isTrue();
            pool.submit(() -> "queued");
            overflow = pool.submit(() -> {                    // no room: runs on this thread
                ranOn.set(Thread.currentThread());
                return REQUEST_ID.get();
            });
        } finally {
            release.countDown();
        }

        assertThat(ranOn.get()).isSameAs(Thread.currentThread());
        assertThat(overflow.get(5, SECONDS)).isEqualTo("ord-7300");
        assertThat(REQUEST_ID.get()).isEqualTo("ord-7300");
    }
}
