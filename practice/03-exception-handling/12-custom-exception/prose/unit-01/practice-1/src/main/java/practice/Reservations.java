package practice;

import java.sql.SQLException;

/**
 * Reserves stock for the lines of an order. The stock table lives in a database, and a reservation
 * that fails there reaches the checkout service as a ReservationFailedException.
 */
public class Reservations {

    /** The stock table. */
    public interface StockStore {
        /** Takes quantity units of sku out of the available stock. */
        void take(String sku, int quantity) throws SQLException;
    }

    /** A reservation that could not be made. */
    public static class ReservationFailedException extends Exception {
        private final String sku;
        private final int quantity;

        public ReservationFailedException(String sku, int quantity, Throwable cause) {
            // TODO: Build the exception for a failed reservation of quantity units of sku, which
            //       failed because of cause. The checkout service reads getSku() and
            //       getQuantity(); the on-call engineer reads only the log.
            super((String) null);
            throw new IllegalStateException("TODO: ReservationFailedException");
        }

        public String getSku() {
            return sku;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    private final StockStore store;

    public Reservations(StockStore store) {
        this.store = store;
    }

    /** Reserves quantity units of sku, or reports why it could not. */
    public void reserve(String sku, int quantity) throws ReservationFailedException {
        // TODO: Take the stock from the store. A failure of the store reaches the caller as a
        //       ReservationFailedException.
        throw new IllegalStateException("TODO: reserve");
    }
}
