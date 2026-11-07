package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Holds seats of a show for customers at the box office. Each hold runs as one transaction on the
 * customer's own connection: it reads how many seats are left, refuses when too few are, and
 * otherwise writes the new count.
 *
 * <p>Table: {@code shows(id INT PRIMARY KEY, seats_left INT NOT NULL)}.
 */
public class SeatHolds {

    /** Runs between the read and the write of every hold. Production passes nothing. */
    public interface Pause {
        void between() throws Exception;
    }

    private final Pause pause;

    public SeatHolds() {
        this(() -> { });
    }

    public SeatHolds(Pause pause) {
        this.pause = pause;
    }

    /** Holds {@code seats} seats of the show: true when held, false when too few were left. */
    public boolean hold(Connection c, int showId, int seats) throws Exception {
        c.setAutoCommit(false);
        int left = seatsLeft(c, showId);
        pause.between();
        if (left < seats) {
            return refuse(c);
        }
        try (PreparedStatement p = c.prepareStatement("UPDATE shows SET seats_left = ? WHERE id = ?")) {
            p.setInt(1, left - seats);
            p.setInt(2, showId);
            p.executeUpdate();
        }
        c.commit();
        return true;
    }

    /** The seats left for the show, which this hold decides on. */
    int seatsLeft(Connection c, int showId) throws SQLException {
        // TODO: Read the seats left for the show. Other customers may be holding seats of the same
        //       show at this very moment, on their own connections.
        throw new IllegalStateException("TODO: seatsLeft");
    }

    /** Ends a hold that found too few seats. Always returns false. */
    boolean refuse(Connection c) throws SQLException {
        // TODO: The customer is told there are not enough seats. Other customers keep buying.
        throw new IllegalStateException("TODO: refuse");
    }
}
