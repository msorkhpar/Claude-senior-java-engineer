package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Catalog implements AutoCloseable {

    /** Thrown when a lazy collection is first read after its catalog was closed. */
    public static final class LazyInitializationException extends RuntimeException {
        public LazyInitializationException(String message) {
            super(message);
        }
    }

    private final Connection connection;
    private boolean closed;

    public Catalog(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    /** The category, with its products not yet loaded. */
    public Optional<Category> category(int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM categories WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                Category category = new Category(rows.getInt("id"), rows.getString("name"));
                category.products();
                return Optional.of(category);
            }
        }
    }

    /** Closes the catalog; the connection stays open. */
    @Override
    public void close() {
        closed = true;
    }

    public final class Category {
        private final int id;
        private final String name;
        private List<String> products;

        private Category(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int id() {
            return id;
        }

        public String name() {
            return name;
        }

        /** The product names, ordered by id: loaded on first call, then kept. */
        public List<String> products() throws SQLException {
            if (products == null) {
                if (closed) {
                    throw new LazyInitializationException("category " + id + ": catalog is closed");
                }
                products = load();
            }
            return products;
        }

        private List<String> load() throws SQLException {
            List<String> names = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT name FROM products WHERE category_id = ? ORDER BY id")) {
                statement.setInt(1, id);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        names.add(rows.getString("name"));
                    }
                }
            }
            return List.copyOf(names);
        }
    }
}
