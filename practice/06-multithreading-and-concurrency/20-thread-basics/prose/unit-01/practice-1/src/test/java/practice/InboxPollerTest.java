package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class InboxPollerTest {

    private final ConcurrentLinkedQueue<String> inbox = new ConcurrentLinkedQueue<>();
    private final List<String> log = new CopyOnWriteArrayList<>();
    private final CountDownLatch handledTwo = new CountDownLatch(2);
    private ExecutorService pool;

    private InboxPoller poller() {
        return new InboxPoller(inbox::poll, line -> {
            log.add(line);
            if (line.contains(" handled ")) handledTwo.countDown();
        });
    }

    /** The service's background pool: daemon threads named support-bg-1, support-bg-2, ... */
    private ExecutorService backgroundPool() {
        AtomicInteger n = new AtomicInteger();
        pool = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "support-bg-" + n.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
        return pool;
    }

    @AfterEach
    void stopThePool() {
        if (pool != null) pool.shutdownNow();
    }

    @Test
    void handlesEachMessageAndStopsWhenItsOwnThreadIsInterrupted() throws InterruptedException {
        inbox.addAll(List.of("ticket-4410: refund", "ticket-4411: password reset"));
        InboxPoller poller = poller();
        poller.setDaemon(true);

        poller.start();
        assertThat(handledTwo.await(5, TimeUnit.SECONDS)).isTrue();
        poller.interrupt();
        poller.join(5_000);

        assertThat(poller.isAlive()).isFalse();
        assertThat(log).containsExactly(
                "inbox-poller handled ticket-4410: refund",
                "inbox-poller handled ticket-4411: password reset",
                "inbox-poller stopped");
    }

    @Test
    void shuttingDownTheBackgroundPoolStopsIt() throws InterruptedException {
        inbox.addAll(List.of("ticket-7001: invoice copy", "ticket-7002: address change"));
        ExecutorService pool = backgroundPool();

        pool.execute(poller());
        assertThat(handledTwo.await(5, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();

        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(log).last().asString().endsWith(" stopped");
    }

    @Test
    void inThePoolItsLinesNameThePoolThread() throws InterruptedException {
        inbox.addAll(List.of("ticket-7101: cancel order", "ticket-7102: late delivery"));
        ExecutorService pool = backgroundPool();

        pool.execute(poller());
        assertThat(handledTwo.await(5, TimeUnit.SECONDS)).isTrue();

        assertThat(log).startsWith(
                "support-bg-1 handled ticket-7101: cancel order",
                "support-bg-1 handled ticket-7102: late delivery");
    }
}
