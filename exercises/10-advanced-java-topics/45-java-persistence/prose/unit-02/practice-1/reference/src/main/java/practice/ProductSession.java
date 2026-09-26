package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    private final Connection connection;
    private final Map<Integer, Product> managed = new HashMap<>();

    public ProductSession(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    /** The product with this id: loaded once per session, then the same instance. */
    public Optional<Product> find(int id) throws SQLException {
        Product known = managed.get(id);
        if (known != null) {
            return Optional.of(known);
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, name, price FROM products WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                Product product = new Product(rows.getInt("id"), rows.getString("name"), rows.getBigDecimal("price"));
                managed.put(id, product);
                return Optional.of(product);
            }
        }
    }
}
