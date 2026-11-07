package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * The help desk's tickets: ticket(id BIGINT GENERATED ALWAYS AS IDENTITY, title VARCHAR(80)) and
 * ticket_tag(ticket_id BIGINT, tag VARCHAR(20)). Many agents open tickets at the same time, each
 * on a connection of their own.
 */
public final class Tickets {

    private Tickets() {}

    /** Opens a ticket with its tags and returns the new ticket's id. */
    public static long open(Connection conn, String title, List<String> tags) throws SQLException {
        long id = insertTicket(conn, title);
        insertTags(conn, id, tags);
        return id;
    }

    /** Inserts the ticket row and returns the id the database gave it. */
    static long insertTicket(Connection conn, String title) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO ticket (title) VALUES (?)")) {
            ps.setString(1, title);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM ticket WHERE title = ?")) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    static void insertTags(Connection conn, long ticketId, List<String> tags) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO ticket_tag (ticket_id, tag) VALUES (?, ?)")) {
            for (String tag : tags) {
                ps.setLong(1, ticketId);
                ps.setString(2, tag);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
