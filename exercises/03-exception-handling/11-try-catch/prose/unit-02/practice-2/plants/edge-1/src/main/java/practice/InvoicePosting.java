package practice;

import java.io.IOException;
import java.sql.SQLException;

/** Posts an invoice to the ledger and archives its PDF, in one database transaction. */
public class InvoicePosting {

    /** The ledger table. */
    public interface Ledger {
        void insert(String invoiceId, long cents) throws SQLException;
    }

    /** The document archive that keeps the invoice's PDF. */
    public interface Archive {
        void store(String invoiceId) throws IOException;
    }

    /** The database transaction the ledger rows are written in. */
    public interface Transaction {
        void begin() throws SQLException;

        void commit() throws SQLException;

        void rollback();
    }

    private final Ledger ledger;
    private final Archive archive;
    private final Transaction tx;

    public InvoicePosting(Ledger ledger, Archive archive, Transaction tx) {
        this.ledger = ledger;
        this.archive = archive;
        this.tx = tx;
    }

    /**
     * Posts one invoice. Either the ledger row is committed and the PDF archived, or the
     * transaction is rolled back and the caller sees what went wrong.
     */
    public void post(String invoiceId, long cents) throws IOException, SQLException {
        tx.begin();
        try {
            write(invoiceId, cents);
        } catch (IOException | SQLException e) {
            tx.rollback();
            throw e;
        }
    }

    /** The work of one posting, inside the transaction. */
    private void write(String invoiceId, long cents) throws IOException, SQLException {
        ledger.insert(invoiceId, cents);
        archive.store(invoiceId);
        tx.commit();
    }
}
