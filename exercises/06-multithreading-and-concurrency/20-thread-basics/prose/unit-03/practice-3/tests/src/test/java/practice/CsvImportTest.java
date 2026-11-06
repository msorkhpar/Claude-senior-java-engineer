package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CsvImportTest {

    /** Records what was staged and committed. */
    static class Recorder implements CsvImport.Store {
        final List<String> staged = Collections.synchronizedList(new ArrayList<>());
        final List<String> committed = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void stage(String importId, List<String> rows) {
            staged.addAll(rows);
        }

        @Override
        public void commit(String importId) {
            committed.add(importId);
        }
    }

    private final ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    private final Recorder store = new Recorder();
    private final CountDownLatch waitingOnStorage = new CountDownLatch(1);
    private final CountDownLatch storageAnswers = new CountDownLatch(1);

    /** Splits a chunk on ';'. The chunk named slow waits on the storage until it answers. */
    private CsvImport.Parser slowOn(String slow) {
        return chunk -> {
            if (chunk.equals(slow)) {
                waitingOnStorage.countDown();
                storageAnswers.await();
            }
            return Arrays.asList(chunk.split(";"));
        };
    }

    @AfterEach
    void cleanUp() {
        storageAnswers.countDown();
        pool.shutdownNow();
    }

    @Test
    void anImportNobodyCancelsCommitsAllItsRows() throws Exception {
        CsvImport imports = new CsvImport(pool, slowOn("none"), store);

        imports.start("imp-5150", List.of("ann;bo", "cy", "dee;eve;fay")).get(5, TimeUnit.SECONDS);

        assertThat(store.staged).containsExactly("ann", "bo", "cy", "dee", "eve", "fay");
        assertThat(store.committed).containsExactly("imp-5150");
    }

    @Test
    void cancelEndsAnImportThatIsWaitingOnTheStorage() throws InterruptedException {
        CsvImport imports = new CsvImport(pool, slowOn("cy;dan"), store);
        Future<?> running = imports.start("imp-6021", List.of("abe;bea", "cy;dan", "eli"));
        assertThat(waitingOnStorage.await(5, TimeUnit.SECONDS)).isTrue();

        assertThat(imports.cancel(running)).isTrue();

        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void anImportCancelledWhileWaitingOnTheStorageCommitsNothing() throws InterruptedException {
        CsvImport imports = new CsvImport(pool, slowOn("cy;dan"), store);
        Future<?> running = imports.start("imp-6022", List.of("abe;bea", "cy;dan", "eli", "flo;gus"));
        assertThat(waitingOnStorage.await(5, TimeUnit.SECONDS)).isTrue();

        imports.cancel(running);

        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(store.committed).isEmpty();
        assertThat(store.staged).containsExactly("abe", "bea");
    }

    @Test
    void aCancelWhileTheLastChunkIsStagedCommitsNothing() throws Exception {
        AtomicReference<Future<?>> running = new AtomicReference<>();
        AtomicReference<CsvImport> imports = new AtomicReference<>();
        Recorder cancelsOnTheLast = new Recorder() {
            @Override
            public void stage(String importId, List<String> rows) {
                super.stage(importId, rows);
                if (rows.contains("zoe")) imports.get().cancel(running.get());   // the user clicks now
            }
        };
        imports.set(new CsvImport(pool, slowOn("yan;zoe"), cancelsOnTheLast));
        running.set(imports.get().start("imp-7300", List.of("wes", "xia", "yan;zoe")));
        assertThat(waitingOnStorage.await(5, TimeUnit.SECONDS)).isTrue();
        storageAnswers.countDown();

        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(cancelsOnTheLast.staged).containsExactly("wes", "xia", "yan", "zoe");
        assertThat(cancelsOnTheLast.committed).isEmpty();
    }
}
