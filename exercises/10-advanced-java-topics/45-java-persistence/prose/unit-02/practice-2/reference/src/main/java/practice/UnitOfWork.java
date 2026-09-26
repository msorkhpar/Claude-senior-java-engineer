package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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

    private record Snapshot(String name, BigDecimal price) {
        static Snapshot of(Product product) {
            return new Snapshot(product.name(), product.price());
        }

        boolean matches(Product product) {
            return Objects.equals(name, product.name()) && samePrice(price, product.price());
        }

        private static boolean samePrice(BigDecimal a, BigDecimal b) {
            return a == null ? b == null : b != null && a.compareTo(b) == 0;
        }
    }

    private final Connection connection;
    private final Map<Product, Snapshot> snapshots = new LinkedHashMap<>();

    public UnitOfWork(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    /** Loads the product and remembers a snapshot of its state. */
    public Product load(int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, name, price FROM products WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalArgumentException("no product " + id);
                }
                Product product = new Product(rows.getInt("id"), rows.getString("name"), rows.getBigDecimal("price"));
                snapshots.put(product, Snapshot.of(product));
                return product;
            }
        }
    }

    /** Writes every product that changed since its snapshot; returns how many were written. */
    public int flush() throws SQLException {
        int written = 0;
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE products SET name = ?, price = ? WHERE id = ?")) {
            for (Map.Entry<Product, Snapshot> entry : snapshots.entrySet()) {
                Product product = entry.getKey();
                if (entry.getValue().matches(product)) {
                    continue;
                }
                update.setString(1, product.name());
                update.setBigDecimal(2, product.price());
                update.setInt(3, product.id());
                update.executeUpdate();
                entry.setValue(Snapshot.of(product));
                written++;
            }
        }
        return written;
    }
}
