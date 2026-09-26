package practice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class Catalog implements AutoCloseable {

    /** Thrown when a lazy collection is first read after its catalog was closed. */
    public static final class LazyInitializationException extends RuntimeException {
        public LazyInitializationException(String message) {
            super(message);
        }
    }

    public Catalog(Connection connection) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The category, with its products not yet loaded. */
    public Optional<Category> category(int id) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }

    /** Closes the catalog; the connection stays open. */
    @Override
    public void close() {
        throw new UnsupportedOperationException("TODO");
    }

    public final class Category {

        public int id() {
            throw new UnsupportedOperationException("TODO");
        }

        public String name() {
            throw new UnsupportedOperationException("TODO");
        }

        /** The product names, ordered by id: loaded on first call, then kept. */
        public List<String> products() throws SQLException {
            throw new UnsupportedOperationException("TODO");
        }
    }
}
