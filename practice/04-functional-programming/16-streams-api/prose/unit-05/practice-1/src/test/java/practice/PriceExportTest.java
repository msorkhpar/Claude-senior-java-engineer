package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PriceExportTest {

    /** The export runs in a pool of four threads the test owns, so it never depends on the runner's CPU count. */
    private final ForkJoinPool pool = new ForkJoinPool(4);
    private final List<String> written = Collections.synchronizedList(new ArrayList<>());

    @AfterEach
    void stopThePool() {
        pool.shutdownNow();
    }

    private static List<String> catalogue(int size) {
        return IntStream.range(0, size).mapToObj(i -> String.format("GTIN-%05d", 40_000 + 7 * i)).toList();
    }

    private static List<String> expectedLines(List<String> catalogue) {
        return catalogue.stream().map(sku -> sku + ";" + sku.length() * 3).toList();
    }

    private void runExport(PriceExport.Pricer pricer, List<String> catalogue) throws Exception {
        PriceExport export = new PriceExport(pricer, written::add);
        pool.submit(() -> export.export(catalogue)).get(20, TimeUnit.SECONDS);
    }

    @Test
    void writesALineForEverySku() throws Exception {
        List<String> catalogue = catalogue(9);

        runExport(sku -> String.valueOf(sku.length() * 3), catalogue);

        assertThat(written).containsExactlyInAnyOrderElementsOf(expectedLines(catalogue));
    }

    @Test
    void pricesSeveralSkusAtOnce() throws Exception {
        CountDownLatch twoAtOnce = new CountDownLatch(2);
        AtomicBoolean pricedAlone = new AtomicBoolean();
        List<String> catalogue = catalogue(40);

        runExport(sku -> {
            twoAtOnce.countDown();
            try {
                if (!twoAtOnce.await(5, TimeUnit.SECONDS)) pricedAlone.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return String.valueOf(sku.length() * 3);
        }, catalogue);

        assertThat(pricedAlone).as("no second SKU was being priced while the first one was").isFalse();
        assertThat(written).containsExactlyInAnyOrderElementsOf(expectedLines(catalogue));
    }

    @Test
    void writesInCatalogueOrderWhenALaterPriceIsReadyFirst() throws Exception {
        List<String> catalogue = catalogue(64);
        String first = catalogue.get(0);
        String last = catalogue.get(catalogue.size() - 1);
        CountDownLatch lastLineWritten = new CountDownLatch(1);

        PriceExport export = new PriceExport(sku -> {
            if (sku.equals(first)) {
                try {
                    lastLineWritten.await(2, TimeUnit.SECONDS);   // the first SKU is the slowest to price
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return String.valueOf(sku.length() * 3);
        }, line -> {
            written.add(line);
            if (line.startsWith(last)) lastLineWritten.countDown();
        });
        pool.submit(() -> export.export(catalogue)).get(20, TimeUnit.SECONDS);

        assertThat(written).containsExactlyElementsOf(expectedLines(catalogue));
    }
}
