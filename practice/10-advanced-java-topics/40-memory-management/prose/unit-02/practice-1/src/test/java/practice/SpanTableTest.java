package practice;

import org.junit.jupiter.api.Test;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

class SpanTableTest {

    /**
     * Asks for collections until the condition holds or ten seconds pass. After each request it
     * waits on a reference queue, so the JDK's reference processing has had its turn.
     */
    private static boolean collectUntil(BooleanSupplier done) throws InterruptedException {
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (done.getAsBoolean()) return true;
            WeakReference<Object> marker = new WeakReference<>(new Object(), queue);
            System.gc();
            queue.remove(200);
            Reference.reachabilityFence(marker);
        }
        return done.getAsBoolean();
    }

    /** Handles one request through the table and lets go of it; returns a way to see whether it is gone. */
    private static WeakReference<SpanTable.Request> handleAndForget(SpanTable table, String id) {
        SpanTable.Request request = new SpanTable.Request(id, "PUT", "/carts/" + id, 256 * 1024);
        table.event(request, "auth ok");
        table.event(request, "cart saved");
        return new WeakReference<>(request);
    }

    /** Opens a span outside the table, as an exporter's copy would be, and lets go of the request. */
    private static SpanTable.Span openAndForget(SpanTable table, WeakReference<?>[] requestOut) {
        SpanTable.Request request = new SpanTable.Request("r-77" + "1", "DELETE", "/sessions/" + "s9", 256 * 1024);
        SpanTable.Span span = table.open(request);
        span.add("session dropped");
        requestOut[0] = new WeakReference<>(request);
        return span;
    }

    @Test
    void recordsEventsAndLabelsPerRequest() {
        SpanTable table = new SpanTable();
        SpanTable.Request a = new SpanTable.Request("r-1" + "01", "POST", "/invoices", 64);
        SpanTable.Request b = new SpanTable.Request("r-1" + "02", "GET", "/invoices/" + 88, 0);

        table.event(a, "validated");
        table.event(b, "cache miss");
        table.event(a, "stored");

        assertThat(table.spanFor(a).events()).containsExactly("validated", "stored");
        assertThat(table.spanFor(b).events()).containsExactly("cache miss");
        assertThat(table.spanFor(a).label()).isEqualTo("POST /invoices");
        assertThat(table.spanFor(b).label()).isEqualTo("GET /invoices/88");
        assertThat(table.spanFor(a)).isSameAs(table.spanFor(a));
        assertThat(table.size()).isEqualTo(2);
    }

    @Test
    void keepsTheSpansOfRequestsStillBeingHandled() throws InterruptedException {
        SpanTable table = new SpanTable();
        SpanTable.Request live = new SpanTable.Request("r-2" + "05", "PATCH", "/users/" + 7, 32);
        table.event(live, "loaded");

        for (int i = 0; i < 3; i++) {
            System.gc();
        }

        assertThat(table.size()).isEqualTo(1);
        assertThat(table.spanFor(live).events()).containsExactly("loaded");
        Reference.reachabilityFence(live);
    }

    @Test
    void aRequestNobodyHoldsAnyMoreLeavesTheTable() throws InterruptedException {
        SpanTable table = new SpanTable();
        SpanTable.Request live = new SpanTable.Request("r-3" + "10", "GET", "/carts/" + "c1", 32);
        table.event(live, "loaded");
        WeakReference<SpanTable.Request> finished = handleAndForget(table, "r-3" + "11");

        boolean gone = collectUntil(() -> finished.get() == null && table.size() == 1);

        assertThat(gone).as("the finished request is collected and its span leaves the table").isTrue();
        assertThat(table.spanFor(live).events()).containsExactly("loaded");
        Reference.reachabilityFence(live);
    }

    @Test
    void aSpanKeptAfterItsRequestDoesNotKeepTheRequest() throws InterruptedException {
        SpanTable table = new SpanTable();
        WeakReference<?>[] request = new WeakReference<?>[1];
        SpanTable.Span kept = openAndForget(table, request);

        boolean collected = collectUntil(() -> request[0].get() == null);

        assertThat(collected).as("the request is collected while its span is still held").isTrue();
        assertThat(kept.label()).isEqualTo("DELETE /sessions/s9");
        assertThat(kept.events()).containsExactly("session dropped");
    }
}
