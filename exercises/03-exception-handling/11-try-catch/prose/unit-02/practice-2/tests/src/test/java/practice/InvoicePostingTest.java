package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoicePostingTest {

    /** Records every call in one list; each step throws what it was told to, if anything. */
    static final class Books implements InvoicePosting.Ledger, InvoicePosting.Archive, InvoicePosting.Transaction {
        final List<String> calls = new ArrayList<>();
        Exception ledgerFails;
        IOException archiveFails;

        @Override
        public void insert(String invoiceId, long cents) throws SQLException {
            calls.add("insert " + invoiceId + " " + cents);
            if (ledgerFails instanceof SQLException e) throw e;
            if (ledgerFails instanceof RuntimeException e) throw e;
        }

        @Override
        public void store(String invoiceId) throws IOException {
            calls.add("store " + invoiceId);
            if (archiveFails != null) throw archiveFails;
        }

        @Override
        public void begin() {
            calls.add("begin");
        }

        @Override
        public void commit() {
            calls.add("commit");
        }

        @Override
        public void rollback() {
            calls.add("rollback");
        }

        InvoicePosting posting() {
            return new InvoicePosting(this, this, this);
        }
    }

    @Test
    void aPostedInvoiceIsCommittedOnce() throws Exception {
        Books books = new Books();

        books.posting().post("INV-30417", 129_900);

        assertThat(books.calls).containsExactly("begin", "insert INV-30417 129900", "store INV-30417", "commit");
    }

    @Test
    void aFailedArchiveRollsTheInvoiceBack() {
        Books books = new Books();
        books.archiveFails = new IOException("archive volume is read-only");

        Throwable thrown = catchThrowable(() -> books.posting().post("INV-30418", 4_250));

        assertThat(thrown).isNotNull();
        assertThat(books.calls).containsExactly("begin", "insert INV-30418 4250", "store INV-30418", "rollback");
    }

    @Test
    void anUnexpectedFailureAlsoRollsBackAndReachesTheCallerUnchanged() {
        Books books = new Books();
        IllegalArgumentException bug = new IllegalArgumentException("currency XTS is not enabled");
        books.ledgerFails = bug;

        Throwable thrown = catchThrowable(() -> books.posting().post("INV-30419", 77_000));

        assertThat(books.calls).containsExactly("begin", "insert INV-30419 77000", "rollback");
        assertThat(thrown).isSameAs(bug);
    }

    @Test
    void aDatabaseFailureReachesTheCallerAsTheSameSqlException() {
        Books books = new Books();
        SQLException conflict = new SQLException("could not serialize access", "40001");
        books.ledgerFails = conflict;

        Throwable thrown = catchThrowable(() -> books.posting().post("INV-30420", 18_300));

        assertThat(thrown).isSameAs(conflict);
        assertThat(books.calls).endsWith("rollback");
    }
}
