package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class CategoryReport {

    private CategoryReport() {
    }

    public record CategoryProducts(String category, List<String> products) {
    }

    /** Every category in id order with its product names in id order, in one query. */
    public static List<CategoryProducts> load(Connection connection) throws SQLException {
        List<CategoryProducts> result = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT id, name FROM categories ORDER BY id");
             PreparedStatement products = connection.prepareStatement(
                     "SELECT name FROM products WHERE category_id = ? ORDER BY id")) {
            while (rows.next()) {
                List<String> names = new ArrayList<>();
                products.setInt(1, rows.getInt("id"));
                try (ResultSet productRows = products.executeQuery()) {
                    while (productRows.next()) {
                        names.add(productRows.getString("name"));
                    }
                }
                result.add(new CategoryProducts(rows.getString("name"), List.copyOf(names)));
            }
        }
        return result;
    }
}
