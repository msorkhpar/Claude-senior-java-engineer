package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteFeedTest {

    private static final long TWO_MINUTES = 120_000;

    private final List<Thread> started = new ArrayList<>();

    @AfterEach
    void stopEveryThread() throws InterruptedException {
        for (Thread t : started) t.interrupt();
        for (Thread t : started) t.join(5_000);
    }

    /** Runs the call on a daemon thread of its own and returns its future. */
    private <T> FutureTask<T> inBackground(String name, Callable<T> call) {
        FutureTask<T> task = new FutureTask<>(call);
        Thread t = new Thread(task, name);
        t.setDaemon(true);
        started.add(t);
        t.start();
        return task;
    }

    /** A connector that fails every attempt, and counts the first one down. */
    private static QuoteFeed.Connector failing(CountDownLatch firstAttempt) {
        return attempt -> {
            firstAttempt.countDown();
            return false;
        };
    }

    private static boolean reaches(Thread thread, Thread.State state) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (thread.getState() != state) {
            if (System.nanoTime() > deadline) return false;
            Thread.yield();
        }
        return true;
    }

    @Test
    void reconnectsOnTheThirdAttempt() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        QuoteFeed feed = new QuoteFeed(attempt -> calls.incrementAndGet() == 3, 2);

        FutureTask<Boolean> reconnect = inBackground("feed", feed::reconnect);

        assertThat(reconnect.get(10, TimeUnit.SECONDS)).isTrue();
        assertThat(calls.get()).isEqualTo(3);
        assertThat(feed.status()).isEqualTo("connected, attempts: 3");
    }

    @Test
    void aClosedFeedDoesNotTryAgain() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        QuoteFeed feed = new QuoteFeed(attempt -> calls.incrementAndGet() > 0, 2);
        feed.close();

        FutureTask<Boolean> reconnect = inBackground("feed", feed::reconnect);

        assertThat(reconnect.get(10, TimeUnit.SECONDS)).isFalse();
        assertThat(calls.get()).isZero();
        assertThat(feed.status()).isEqualTo("closed, attempts: 0");
    }

    @Test
    void theMonitoringPageGetsItsStatusDuringABackOff() throws Exception {
        CountDownLatch firstAttempt = new CountDownLatch(1);
        QuoteFeed feed = new QuoteFeed(failing(firstAttempt), TWO_MINUTES);
        inBackground("feed", feed::reconnect);
        Thread feedThread = started.get(0);
        assertThat(firstAttempt.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(reaches(feedThread, Thread.State.TIMED_WAITING)).as("the feed is backing off").isTrue();

        FutureTask<String> status = inBackground("monitoring", feed::status);

        assertThat(status.get(5, TimeUnit.SECONDS)).isEqualTo("reconnecting, attempts: 1");
    }

    @Test
    void closingEndsABackOffInProgress() throws Exception {
        CountDownLatch firstAttempt = new CountDownLatch(1);
        QuoteFeed feed = new QuoteFeed(failing(firstAttempt), TWO_MINUTES);
        FutureTask<Boolean> reconnect = inBackground("feed", feed::reconnect);
        assertThat(firstAttempt.await(5, TimeUnit.SECONDS)).isTrue();

        FutureTask<Void> close = inBackground("shutdown", () -> { feed.close(); return null; });

        close.get(5, TimeUnit.SECONDS);
        assertThat(reconnect.get(5, TimeUnit.SECONDS)).isFalse();
        assertThat(feed.status()).isEqualTo("closed, attempts: 1");
    }
}
