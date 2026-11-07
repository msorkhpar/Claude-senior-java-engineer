package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Builds the query behind the support tool's order search. The support agent types a customer's
 * e-mail address and may pick a status; the sort column comes from the page's URL (?sort=...).
 * The caller hands sql() to Connection.prepareStatement and binds params() in order.
 */
public class OrderSearch {

    /** A statement and the values to bind to its placeholders, in order. */
    public record Query(String sql, List<Object> params) {}

    /** The columns the result page offers to sort by. */
    static final Set<String> SORTABLE = Set.of("placed_at", "total", "status");

    /** status and sortColumn may be null: no status filter, and sorting by placed_at. */
    public Query search(String customerEmail, String status, String sortColumn) {
        List<Object> params = new ArrayList<>();
        String where = condition("customer_email", customerEmail, params);
        if (status != null) {
            where += "\n  AND " + condition("status", status, params);
        }
        String sql = """
                SELECT id, customer_email, status, total
                FROM orders
                WHERE %s
                ORDER BY %s
                """.formatted(where, orderBy(sortColumn));
        return new Query(sql, List.copyOf(params));
    }

    /**
     * The condition that compares column with value, such as the one on customer_email.
     * The value is whatever the support agent typed.
     */
    String condition(String column, String value, List<Object> params) {
        return column + " = '" + value.replace("'", "''") + "'";
    }

    /** The column to sort by: sortColumn as it came from the URL, or placed_at when it is null. */
    String orderBy(String sortColumn) {
        if (sortColumn == null) return "placed_at";
        if (!SORTABLE.contains(sortColumn)) {
            throw new IllegalArgumentException("cannot sort by " + sortColumn);
        }
        return sortColumn;
    }
}
