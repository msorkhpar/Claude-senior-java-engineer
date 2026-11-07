package practice;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ReportServiceTest {

    /** Requests arrive the way the web server delivers them: each on a virtual thread of its own. */
    private static List<Future<byte[]>> request(ExecutorService requests, ReportService service, String... ids) {
        List<Future<byte[]>> replies = new ArrayList<>();
        for (String id : ids) replies.add(requests.submit(() -> service.handle(id)));
        return replies;
    }

    private static String text(Future<byte[]> reply) throws Exception {
        return new String(reply.get(10, TimeUnit.SECONDS), StandardCharsets.UTF_8);
    }

    /**
     * A renderer whose renders stay in progress until the test releases them. It notes when
     * `target` renders are in progress at once, and when more than `limit` are.
     */
    private static final class HeldRenderer implements ReportService.Renderer {
        final AtomicInteger inside = new AtomicInteger();
        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch reached = new CountDownLatch(1);
        final CountDownLatch exceeded = new CountDownLatch(1);
        final int target;
        final int limit;

        HeldRenderer(int target, int limit) {
            this.target = target;
            this.limit = limit;
        }

        @Override
        public byte[] render(String reportId) {
            int now = inside.incrementAndGet();
            if (now >= target) reached.countDown();
            if (now > limit) exceeded.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            inside.decrementAndGet();
            return ("pdf:" + reportId).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Test
    void everyRequestGetsItsOwnReport() throws Exception {
        ExecutorService requests = Executors.newVirtualThreadPerTaskExecutor();
        ReportService.Renderer renderer = id -> ("pdf:" + id).getBytes(StandardCharsets.UTF_8);
        try (ReportService service = new ReportService(renderer, 2)) {
            List<Future<byte[]>> replies = request(requests, service,
                    "q3-sales", "q3-churn", "emea-tax", "apac-tax", "fleet-usage", "audit-2031");
            assertThat(text(replies.get(0))).isEqualTo("pdf:q3-sales");
            assertThat(text(replies.get(1))).isEqualTo("pdf:q3-churn");
            assertThat(text(replies.get(2))).isEqualTo("pdf:emea-tax");
            assertThat(text(replies.get(3))).isEqualTo("pdf:apac-tax");
            assertThat(text(replies.get(4))).isEqualTo("pdf:fleet-usage");
            assertThat(text(replies.get(5))).isEqualTo("pdf:audit-2031");
        } finally {
            requests.shutdownNow();
        }
    }

    @Test
    void rendersNeverRunOnARequestsVirtualThread() throws Exception {
        ExecutorService requests = Executors.newVirtualThreadPerTaskExecutor();
        Map<String, Boolean> onVirtual = new ConcurrentHashMap<>();
        ReportService.Renderer renderer = id -> {
            onVirtual.put(id, Thread.currentThread().isVirtual());
            return id.getBytes(StandardCharsets.UTF_8);
        };
        try (ReportService service = new ReportService(renderer, 3)) {
            for (Future<byte[]> reply : request(requests, service, "inv-11", "inv-12", "inv-13", "inv-14")) {
                text(reply);
            }
            assertThat(onVirtual).hasSize(4);
            assertThat(onVirtual.values()).containsOnly(false);
        } finally {
            requests.shutdownNow();
        }
    }

    @Test
    void neverMoreRendersAtOnceThanCores() throws Exception {
        ExecutorService requests = Executors.newVirtualThreadPerTaskExecutor();
        HeldRenderer twoInside = new HeldRenderer(2, 2);
        try (ReportService service = new ReportService(twoInside, 2)) {
            List<Future<byte[]>> replies = request(requests, service, "r-1", "r-2", "r-3", "r-4", "r-5", "r-6");
            assertThat(twoInside.reached.await(10, TimeUnit.SECONDS)).as("two renders started").isTrue();
            assertThat(twoInside.exceeded.await(500, TimeUnit.MILLISECONDS)).as("a third render started while two ran").isFalse();
            twoInside.release.countDown();
            for (Future<byte[]> reply : replies) text(reply);
        } finally {
            twoInside.release.countDown();
            requests.shutdownNow();
        }
    }

    @Test
    void asManyRendersAtOnceAsCores() throws Exception {
        ExecutorService requests = Executors.newVirtualThreadPerTaskExecutor();
        HeldRenderer threeInside = new HeldRenderer(3, Integer.MAX_VALUE);
        try (ReportService service = new ReportService(threeInside, 3)) {
            List<Future<byte[]>> replies = request(requests, service, "m-1", "m-2", "m-3");
            assertThat(threeInside.reached.await(5, TimeUnit.SECONDS)).as("three renders ran at once").isTrue();
            threeInside.release.countDown();
            for (Future<byte[]> reply : replies) text(reply);
        } finally {
            threeInside.release.countDown();
            requests.shutdownNow();
        }
    }
}
