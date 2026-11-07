package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookSenderTest {

    /**
     * Pauses without waiting, and behaves like Thread.sleep towards interrupts: a thread whose
     * flag is set gets InterruptedException at once, with the flag cleared. When deployAt is n,
     * a deploy arrives during the n-th pause.
     */
    static final class Pauses implements WebhookSender.Pause {
        final List<Long> taken = Collections.synchronizedList(new ArrayList<>());
        final int deployAt;

        Pauses(int deployAt) {
            this.deployAt = deployAt;
        }

        @Override
        public void pause(long millis) throws InterruptedException {
            taken.add(millis);
            if (Thread.interrupted()) throw new InterruptedException();
            if (taken.size() == deployAt) throw new InterruptedException();
        }
    }

    private final ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void cleanUp() {
        pool.shutdownNow();
        Thread.interrupted();
    }

    @Test
    void retriesARejectedPayloadAndDeliversTheOnesAfterIt() throws InterruptedException {
        BlockingQueue<String> outbox = new LinkedBlockingQueue<>(List.of("invoice.paid#301", "invoice.paid#302"));
        List<String> accepted = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch both = new CountDownLatch(2);
        AtomicInteger rejections = new AtomicInteger(2);
        WebhookSender.Endpoint flaky = payload -> {
            if (payload.endsWith("#301") && rejections.getAndDecrement() > 0) return false;
            accepted.add(payload);
            both.countDown();
            return true;
        };
        Pauses pauses = new Pauses(0);

        pool.execute(new WebhookSender(outbox, flaky, pauses, 5));

        assertThat(both.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(accepted).containsExactly("invoice.paid#301", "invoice.paid#302");
        assertThat(pauses.taken).containsExactly(200L, 400L);
    }

    @Test
    void aDeployDuringAPauseMeansNoMoreAttempts() {
        AtomicInteger posts = new AtomicInteger();
        WebhookSender sender = new WebhookSender(new LinkedBlockingQueue<>(),
                payload -> { posts.incrementAndGet(); return false; }, new Pauses(2), 6);

        boolean delivered = sender.deliver("refund.created#88");

        assertThat(delivered).isFalse();
        assertThat(posts.get()).isEqualTo(2);
    }

    @Test
    void aDeployDuringAPauseIsStillVisibleAfterTheDeliveryGivesUp() {
        WebhookSender sender = new WebhookSender(new LinkedBlockingQueue<>(),
                payload -> false, new Pauses(1), 6);

        sender.deliver("refund.created#89");

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }

    @Test
    void aDeployWhileTheOutboxIsEmptyStopsTheSender() throws InterruptedException {
        CountDownLatch waiting = new CountDownLatch(1);
        BlockingQueue<String> outbox = new LinkedBlockingQueue<>() {
            @Override
            public String take() throws InterruptedException {
                waiting.countDown();
                return super.take();
            }
        };
        pool.execute(new WebhookSender(outbox, payload -> true, new Pauses(0), 5));
        assertThat(waiting.await(5, TimeUnit.SECONDS)).isTrue();

        pool.shutdownNow();

        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    }
}
