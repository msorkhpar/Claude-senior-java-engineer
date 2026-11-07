package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceExportTest {

    private static final List<String> INVOICES =
            IntStream.rangeClosed(1, 24).mapToObj(i -> String.format("INV-2031-%03d", i)).toList();

    /** A parallel stream started from a pool's worker runs in that pool: four workers, whatever the machine. */
    private final ForkJoinPool pool = new ForkJoinPool(4);

    @AfterEach
    void stopThePool() {
        pool.shutdownNow();
    }

    private void exportOnPool(Function<String, String> render, Consumer<String> writer) throws Exception {
        pool.submit(() -> InvoiceExport.export(INVOICES, render, writer)).get(20, TimeUnit.SECONDS);
    }

    private static String line(String id) {
        return id + ";EUR;" + id.hashCode() % 1000;
    }

    private static List<String> allLines() {
        return INVOICES.stream().map(InvoiceExportTest::line).toList();
    }

    @Test
    void everyInvoiceReachesTheFile() throws Exception {
        List<String> file = Collections.synchronizedList(new ArrayList<>());

        exportOnPool(InvoiceExportTest::line, file::add);

        assertThat(file).containsExactlyInAnyOrderElementsOf(allLines());
    }

    @Test
    void theWriterIsNeverUsedByTwoThreadsAtOnce() throws Exception {
        AtomicInteger inside = new AtomicInteger();
        AtomicBoolean overlapped = new AtomicBoolean();
        CyclicBarrier secondWriter = new CyclicBarrier(2);
        List<String> file = Collections.synchronizedList(new ArrayList<>());
        Consumer<String> writer = text -> {
            if (inside.incrementAndGet() > 1) overlapped.set(true);
            try {
                // Hold the writer until a second thread comes in, or give up after two seconds.
                secondWriter.await(2, TimeUnit.SECONDS);
            } catch (TimeoutException | BrokenBarrierException e) {
                // nobody else came: the expected case
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                inside.decrementAndGet();
            }
            file.add(text);
        };

        exportOnPool(InvoiceExportTest::line, writer);

        assertThat(overlapped).isFalse();
    }

    @Test
    void theFileListsTheInvoicesInOrderEvenWhenTheFirstRendersLast() throws Exception {
        CountDownLatch lastRendered = new CountDownLatch(1);
        String firstId = INVOICES.get(0);
        String lastId = INVOICES.get(INVOICES.size() - 1);
        Function<String, String> render = id -> {
            if (id.equals(firstId)) {
                try {
                    lastRendered.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            String text = line(id);
            if (id.equals(lastId)) lastRendered.countDown();
            return text;
        };
        List<String> file = Collections.synchronizedList(new ArrayList<>());

        exportOnPool(render, file::add);

        assertThat(file).containsExactlyElementsOf(allLines());
    }

    @Test
    void aSlowInvoiceDoesNotHoldUpTheOthers() throws Exception {
        CountDownLatch lastRendered = new CountDownLatch(1);
        AtomicBoolean othersWentOn = new AtomicBoolean();
        String firstId = INVOICES.get(0);
        String lastId = INVOICES.get(INVOICES.size() - 1);
        Function<String, String> render = id -> {
            if (id.equals(firstId)) {
                try {
                    othersWentOn.set(lastRendered.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (id.equals(lastId)) lastRendered.countDown();
            return line(id);
        };
        List<String> file = Collections.synchronizedList(new ArrayList<>());

        exportOnPool(render, file::add);

        assertThat(othersWentOn).isTrue();
    }
}
