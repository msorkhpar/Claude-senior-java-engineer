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
            super("reservation failed", cause);
            this.sku = sku;
            this.quantity = quantity;
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
        try {
            store.take(sku, quantity);
        } catch (SQLException e) {
            throw new ReservationFailedException(sku, quantity, e);
        }
    }
}
