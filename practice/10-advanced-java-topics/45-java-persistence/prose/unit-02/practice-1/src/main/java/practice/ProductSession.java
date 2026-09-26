package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public final class ProductSession {

    /** A managed entity: mutable, one instance per id within a session. */
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

    public ProductSession(Connection connection) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The product with this id: loaded once per session, then the same instance. */
    public Optional<Product> find(int id) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
