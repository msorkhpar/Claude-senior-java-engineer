package practice;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class LabelPrinterTest {

    private static final LabelPrinter.Cities DIRECTORY = postcode -> switch (postcode) {
        case "69002" -> "Lyon";
        case "4050-123" -> "Porto";
        case "10115" -> "Berlin";
        case "1012 AB" -> "Amsterdam";
        default -> "Unknown";
    };

    /** Runs the call on a daemon thread of its own, so a call that never returns can't hang the run. */
    private static <T> FutureTask<T> started(Callable<T> call) {
        FutureTask<T> task = new FutureTask<>(call);
        Thread t = new Thread(task, "packer");
        t.setDaemon(true);
        t.start();
        return task;
    }

    @Test
    void rendersEachLabelAndCountsIt() throws Exception {
        LabelPrinter printer = new LabelPrinter(DIRECTORY);
        long before = LabelPrinter.renderedByAll();

        String first = started(() -> printer.render("Lena Vogel", "Invalidenstr. 7", "10115")).get(5, TimeUnit.SECONDS);
        String second = started(() -> printer.render("Joost de Wit", "Damrak 1", "1012 AB")).get(5, TimeUnit.SECONDS);

        assertThat(first).isEqualTo("Lena Vogel\nInvalidenstr. 7\n10115 Berlin");
        assertThat(second).isEqualTo("Joost de Wit\nDamrak 1\n1012 AB Amsterdam");
        assertThat(LabelPrinter.renderedByAll() - before).isEqualTo(2);
    }

    @Test
    void aLabelRenderedWhileAnotherWaitsForItsCityKeepsItsOwnLines() throws Exception {
        CountDownLatch slowLookupStarted = new CountDownLatch(1);
        CountDownLatch otherLabelDone = new CountDownLatch(1);
        LabelPrinter printer = new LabelPrinter(postcode -> {
            if (postcode.equals("69002")) {
                slowLookupStarted.countDown();
                try {
                    otherLabelDone.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return DIRECTORY.cityOf(postcode);
        });

        FutureTask<String> slow = started(() -> printer.render("Claire Martin", "3 Rue Merciere", "69002"));
        assertThat(slowLookupStarted.await(5, TimeUnit.SECONDS)).isTrue();
        FutureTask<String> fast = started(() -> printer.render("Rui Costa", "Rua das Flores 20", "4050-123"));
        String fastLabel = fast.get(5, TimeUnit.SECONDS);
        otherLabelDone.countDown();

        assertThat(fastLabel).isEqualTo("Rui Costa\nRua das Flores 20\n4050-123 Porto");
        assertThat(slow.get(5, TimeUnit.SECONDS)).isEqualTo("Claire Martin\n3 Rue Merciere\n69002 Lyon");
    }

    @Test
    void theCountCoversEveryPrinterNotJustTheNewestOne() throws Exception {
        LabelPrinter north = new LabelPrinter(DIRECTORY);
        long before = LabelPrinter.renderedByAll();
        north.render("Lena Vogel", "Invalidenstr. 7", "10115");
        north.render("Rui Costa", "Rua das Flores 20", "4050-123");

        LabelPrinter south = new LabelPrinter(DIRECTORY);
        south.render("Joost de Wit", "Damrak 1", "1012 AB");
        south.render("Claire Martin", "3 Rue Merciere", "69002");
        south.render("Lena Vogel", "Invalidenstr. 7", "10115");

        assertThat(LabelPrinter.renderedByAll() - before).isEqualTo(5);
    }
}
