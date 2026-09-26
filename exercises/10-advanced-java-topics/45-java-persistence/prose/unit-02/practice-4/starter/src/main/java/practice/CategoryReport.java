package practice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class CategoryReport {

    private CategoryReport() {
    }

    public record CategoryProducts(String category, List<String> products) {
    }

    /** Every category in id order with its product names in id order, in one query. */
    public static List<CategoryProducts> load(Connection connection) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
