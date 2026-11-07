package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class InvoiceExportTest {

    private static final List<String> INVOICES = IntStream.rangeClosed(1, 40)
            .mapToObj(i -> String.format("INV-%04d", 300 + i)).toList();
    private static final Map<String, Long> TAX = INVOICES.stream()
            .collect(Collectors.toMap(i -> i, i -> 125L * Integer.parseInt(i.substring(4))));

    /** The pool each lookup ran in; "none" when it ran on a thread outside any pool. */
    private final Set<Object> poolsSeen = ConcurrentHashMap.newKeySet();

    /** The caller: a plain thread, outside any pool, so a call that never returns can't hang the test. */
    private final ExecutorService caller = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "export-caller");
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void stopTheCaller() {
        caller.shutdownNow();
    }

    private long recordingLookup(String invoice) {
        ForkJoinPool pool = ForkJoinTask.getPool();
        poolsSeen.add(pool == null ? "none" : pool);
        if (invoice.equals("INV-0317-BAD")) throw new IllegalArgumentException("no tax code on " + invoice);
        return TAX.get(invoice);
    }

    private <T> T call(Callable<T> action) throws Exception {
        try {
            return caller.submit(action).get(10, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw (Exception) e.getCause();
        }
    }

    @Test
    void addsUpTheTaxOfEveryInvoice() throws Exception {
        InvoiceExport export = new InvoiceExport(this::recordingLookup, 3);

        long total = call(() -> export.totalTax(INVOICES));

        assertThat(total).isEqualTo(TAX.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void noLookupThreadsAreLeftBehind() throws Exception {
        InvoiceExport export = new InvoiceExport(this::recordingLookup, 2);

        call(() -> export.totalTax(INVOICES));

        assertThat(poolsSeen).allSatisfy(pool ->
                assertThat(pool).isInstanceOfSatisfying(ForkJoinPool.class,
                        p -> assertThat(p.isShutdown()).as("the lookups' pool is shut down").isTrue()));
    }

    @Test
    void noLookupThreadsAreLeftBehindWhenALookupFails() throws Exception {
        InvoiceExport export = new InvoiceExport(this::recordingLookup, 2);
        List<String> withABadOne = new java.util.ArrayList<>(INVOICES);
        withABadOne.add(17, "INV-0317-BAD");

        Throwable thrown = catchThrowable(() -> call(() -> export.totalTax(withABadOne)));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(poolsSeen).allSatisfy(pool ->
                assertThat(pool).isInstanceOfSatisfying(ForkJoinPool.class,
                        p -> assertThat(p.isShutdown()).as("the lookups' pool is shut down").isTrue()));
    }
}
