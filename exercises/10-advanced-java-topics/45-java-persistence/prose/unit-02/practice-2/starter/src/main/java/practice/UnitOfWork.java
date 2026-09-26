package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public final class UnitOfWork {

    /** A managed entity: mutable, tracked by the unit of work that loaded it. */
    public static final class Product {
        private final int id;
        private String name;
        private BigDecimal price;

        public Product(int id, String name, BigDecimal price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public int id() { return id; }
        public String name() { return name; }
        public BigDecimal price() { return price; }
        public void setName(String name) { this.name = name; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    public UnitOfWork(Connection connection) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Loads the product and remembers a snapshot of its state. */
    public Product load(int id) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }

    /** Writes every product that changed since its snapshot; returns how many were written. */
    public int flush() throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
