package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Adds tags to products in the shop's back office. Editors work without locks: an editor opens a
 * product, adds a tag and saves, and other editors and jobs may change the same product meanwhile.
 *
 * <p>Table: {@code product(id INT PRIMARY KEY, tags VARCHAR(500) NOT NULL, version INT NOT NULL)};
 * {@code tags} is a comma-separated list in the order the tags were added.
 */
public class ProductTags {

    /** Runs after every read of a product. Production passes nothing. */
    public interface Pause {
        void between() throws Exception;
    }

    /** How many times one call tries to save before it gives up. */
    static final int ATTEMPTS = 3;

    private final Pause pause;

    public ProductTags() {
        this(() -> { });
    }

    public ProductTags(Pause pause) {
        this.pause = pause;
    }

    record Product(String tags, int version) { }

    /** Adds the tag to the product: true once saved, false if every attempt lost to another writer. */
    public boolean addTag(Connection c, int productId, String tag) throws Exception {
        c.setAutoCommit(false);
        Product read = load(c, productId);
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            if (save(c, productId, withTag(read.tags(), tag), read.version())) {
                c.commit();
                return true;
            }
            c.rollback();
        }
        return false;
    }

    /** Writes the product's new tags. True if they were written. */
    boolean save(Connection c, int productId, String tags, int versionRead) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE product SET tags = ?, version = version + 1 WHERE id = ? AND version = ?")) {
            p.setString(1, tags);
            p.setInt(2, productId);
            p.setInt(3, versionRead);
            return p.executeUpdate() == 1;
        }
    }

    /** Reads the product as it is now. Every read of a product goes through here. */
    Product load(Connection c, int productId) throws Exception {
        Product product;
        try (PreparedStatement p = c.prepareStatement("SELECT tags, version FROM product WHERE id = ?")) {
            p.setInt(1, productId);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                product = new Product(r.getString(1), r.getInt(2));
            }
        }
        pause.between();
        return product;
    }

    static String withTag(String tags, String tag) {
        return tags.isEmpty() ? tag : tags + "," + tag;
    }
}
