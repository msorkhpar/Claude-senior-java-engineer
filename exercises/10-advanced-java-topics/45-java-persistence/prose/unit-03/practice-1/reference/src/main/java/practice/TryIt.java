package practice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
        // Turn the logger up, so the log lines of your code show under the printed lines.
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s %5$s%n");
        // Only this handler prints, and it leaves out the JDK's own detail.
        Logger root = Logger.getLogger("");
        for (java.util.logging.Handler before : root.getHandlers()) {
            root.removeHandler(before);
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFilter(entry -> !String.valueOf(entry.getLoggerName()).matches("(java|javax|jdk|sun|com\\.sun)\\..*"));
        root.setLevel(Level.ALL);
        root.addHandler(handler);
        JDK_EXIT.setLevel(Level.OFF);

        // A stand-in for the collaborator, like the one the tests use for the first main case.
        try {
            try (Connection office = h2("theatre;DB_CLOSE_DELAY=-1");
                 Connection north = h2("theatre;LOCK_TIMEOUT=5000"); Connection south = h2("theatre;LOCK_TIMEOUT=5000");
                 Connection east = h2("theatre;LOCK_TIMEOUT=400")) {
                try (Statement st = office.createStatement()) {
                    st.execute("CREATE TABLE shows (id INT PRIMARY KEY, seats_left INT NOT NULL)");
                    st.execute("INSERT INTO shows VALUES (7, 2), (8, 1)");
                }
                boolean[] southGot = new boolean[1];
                Thread southCounter = new Thread(() -> {
                    try {
                        southGot[0] = new SeatHolds().hold(south, 7, 2);
                    } catch (Exception e) {
                        System.out.println("south counter failed: " + e);
                    }
                });
                SeatHolds northBox = new SeatHolds(() -> {
                    southCounter.start();
                    southCounter.join(300);
                });
                boolean northGot = northBox.hold(north, 7, 2);
                southCounter.join();
                System.out.println("show 7, north counter got 2 seats: " + northGot);
                System.out.println("show 7, south counter got 2 seats: " + southGot[0]);
                System.out.println("show 7 seats left: " + seatsLeft(office, 7));
                System.out.println("show 8, north asks for 2: " + new SeatHolds().hold(north, 8, 2));
                System.out.println("show 8, east asks for 1 right after: " + new SeatHolds().hold(east, 8, 1));
                System.out.println("show 8 seats left: " + seatsLeft(office, 8));
                try (Statement st = office.createStatement()) {
                    st.execute("SHUTDOWN");
                }
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static int seatsLeft(Connection c, int show) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT seats_left FROM shows WHERE id = " + show)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** The practice's database, H2 in memory: Run sees the tests' libraries too. */
    private static Connection h2(String name) throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:" + name);
    }
}
