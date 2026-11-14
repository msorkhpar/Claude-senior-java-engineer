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
            try (Connection db = h2("support")) {
                try (Statement st = db.createStatement()) {
                    st.execute("CREATE TABLE orders (id BIGINT PRIMARY KEY, customer VARCHAR(20), item VARCHAR(40), "
                            + "quantity VARCHAR(10), status VARCHAR(10))");
                    st.execute("INSERT INTO orders VALUES (12, 'c-77', 'stapler', '3', 'OPEN'), "
                            + "(15, 'c-77', 'monitor arm', '1', 'SHIPPED'), (19, 'c-77', 'labels', '25', 'OPEN'), "
                            + "(30, 'c-90', 'whiteboard', 'two', 'OPEN')");
                }
                System.out.println("c-77: " + OrderQueries.openOrders(db, "c-77"));
                System.out.println("c-12: " + OrderQueries.openOrders(db, "c-12"));
                try {
                    OrderQueries.openOrders(db, "c-90");
                } catch (SQLException unreadable) {
                    System.out.println("c-90 refused: " + unreadable.getClass().getSimpleName());
                }
                try (Statement st = db.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM orders")) {
                    rs.next();
                    System.out.println("connection still usable, orders: " + rs.getInt(1));
                }
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** The practice's database, H2 in memory: Run sees the tests' libraries too. */
    private static Connection h2(String name) throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:" + name);
    }
}
