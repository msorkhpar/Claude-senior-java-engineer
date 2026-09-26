package practice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CategoryReport {

    private CategoryReport() {
    }

    public record CategoryProducts(String category, List<String> products) {
    }

    /** Every category in id order with its product names in id order, in one query. */
    public static List<CategoryProducts> load(Connection connection) throws SQLException {
        String sql = """
                SELECT c.id AS category_id, c.name AS category, p.name AS product
                FROM categories c
                LEFT JOIN products p ON p.category_id = c.id
                ORDER BY c.id, p.id
                """;
        Map<Integer, String> names = new LinkedHashMap<>();
        Map<Integer, List<String>> products = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            while (rows.next()) {
                int id = rows.getInt("category_id");
                names.putIfAbsent(id, rows.getString("category"));
                List<String> list = products.computeIfAbsent(id, key -> new ArrayList<>());
                String product = rows.getString("product");
                if (product != null) {
                    list.add(product);
                }
            }
        }
        List<CategoryProducts> result = new ArrayList<>();
        names.forEach((id, name) -> result.add(new CategoryProducts(name, List.copyOf(products.get(id)))));
        return result;
    }
}
