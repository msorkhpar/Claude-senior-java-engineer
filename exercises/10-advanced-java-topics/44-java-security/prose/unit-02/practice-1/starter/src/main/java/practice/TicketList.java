package practice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * The text after ORDER BY for the caller's sort key and direction. The API documents three
     * sort keys: "priority" (column priority), "opened" (column opened_at) and "title" (column
     * title), and two directions, "asc" and "desc". Any other request is refused with an
     * IllegalArgumentException before a query runs.
     */
    static String orderBy(String sort, String dir) {
        // TODO: Both values come straight from the request's query string. Return the text that
        //       goes after ORDER BY, so that only the documented sorts can ever reach the database.
        throw new IllegalStateException("TODO: orderBy");
    }
}
