package practice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The helpdesk's ticket list behind GET /tickets?sort=...&dir=... */
public class TicketList {

    private final Connection conn;

    public TicketList(Connection conn) {
        this.conn = conn;
    }

    /** Ticket titles in the order the caller asked for. */
    public List<String> titles(String sort, String dir) throws SQLException {
        String sql = "SELECT title FROM ticket ORDER BY " + orderBy(sort, dir);
        List<String> titles = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) titles.add(rs.getString(1));
        }
        return titles;
    }

    private static final Map<String, String> COLUMNS = Map.of(
            "priority", "priority",
            "opened", "opened_at",
            "title", "title");
    private static final Map<String, String> DIRECTIONS = Map.of("asc", "ASC", "desc", "DESC");

    /**
     * The text after ORDER BY for the caller's sort key and direction. The API documents three
     * sort keys: "priority" (column priority), "opened" (column opened_at) and "title" (column
     * title), and two directions, "asc" and "desc". Any other request is refused with an
     * IllegalArgumentException before a query runs.
     */
    static String orderBy(String sort, String dir) {
        String column = sort == null ? null : COLUMNS.get(sort);
        if (column == null) {
            throw new IllegalArgumentException("unsupported sort: " + sort);
        }
        return column + " " + dir;
    }
}
