package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ReconciliationExportTest {

    private final ReentrantLock lock = new ReentrantLock();
    private final ReconciliationExport export = new ReconciliationExport(lock);

    /** A writer that records what reaches it; its close() throws {@code onClose} when that is set. */
    private static final class Writer implements ReconciliationExport.ReportWriter {
        final List<String> rows = new ArrayList<>();
        final IOException onClose;
        boolean closed;

        Writer(IOException onClose) {
            this.onClose = onClose;
        }

        @Override
        public void write(String row) {
            rows.add(row);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            if (onClose != null) throw onClose;
        }
    }

    private final IllegalStateException lockWait =
            new IllegalStateException("ledger query cancelled: lock wait timeout after 50 s");

    private ReconciliationExport.Query failing() {
        return () -> {
            throw lockWait;
        };
    }

    private static ReconciliationExport.Query rows() {
        return () -> List.of("ACC-1001;EUR;1200.00", "ACC-1002;EUR;-75.40");
    }

    @Test
    void writesTheRowsThenClosesTheFileAndFreesTheLock() throws IOException {
        Writer out = new Writer(null);

        export.run(rows(), out);

        assertThat(out.rows).containsExactly("ACC-1001;EUR;1200.00", "ACC-1002;EUR;-75.40");
        assertThat(out.closed).isTrue();
        assertThat(lock.isLocked()).isFalse();
    }

    @Test
    void aFailedQueryReachesTheCallerAndStillFreesTheLock() {
        Writer out = new Writer(null);

        Throwable thrown = catchThrowable(() -> export.run(failing(), out));

        assertThat(thrown).isSameAs(lockWait);
        assertThat(out.closed).isTrue();
        assertThat(lock.isLocked()).isFalse();
    }

    @Test
    void whenTheQueryAndTheCloseBothFailTheCallerSeesTheQueryFailure() {
        Writer out = new Writer(new IOException("quota exceeded on /exports"));

        Throwable thrown = catchThrowable(() -> export.run(failing(), out));

        assertThat(thrown).isSameAs(lockWait);
    }

    @Test
    void aCloseThatFailsOnItsOwnStillFreesTheLock() {
        Writer out = new Writer(new IOException("quota exceeded on /exports"));

        catchThrowable(() -> export.run(rows(), out));

        assertThat(lock.isLocked()).isFalse();
    }

    @Test
    void aCloseThatFailsOnItsOwnReachesTheCaller() {
        IOException quota = new IOException("quota exceeded on /exports");
        Writer out = new Writer(quota);

        Throwable thrown = catchThrowable(() -> export.run(rows(), out));

        assertThat(thrown).isSameAs(quota);
    }

    @Test
    void whenBothFailTheCloseFailureTravelsWithTheQueryFailure() {
        IllegalStateException timeout = new IllegalStateException("ledger query cancelled: statement timeout");
        IOException quota = new IOException("quota exceeded on /exports");
        Writer out = new Writer(quota);

        Throwable thrown = catchThrowable(() -> export.run(() -> {
            throw timeout;
        }, out));

        assertThat(thrown).isSameAs(timeout);
        assertThat(timeout.getSuppressed()).containsExactly(quota);
        assertThat(lock.isLocked()).isFalse();
    }
}
