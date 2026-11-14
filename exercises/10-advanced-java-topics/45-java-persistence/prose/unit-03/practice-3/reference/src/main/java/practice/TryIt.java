package practice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
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
            try (Connection importer = h2("partner;DB_CLOSE_DELAY=-1"); Connection warehouse = h2("partner")) {
                try (Statement st = warehouse.createStatement()) {
                    st.execute("CREATE TABLE orders (id INT PRIMARY KEY)");
                    st.execute("CREATE TABLE order_line (order_id INT NOT NULL, sku VARCHAR(12) NOT NULL)");
                }
                List<OrderImport.Order> night = List.of(
                        new OrderImport.Order(801, Arrays.asList("LAMP-3", "BULB-9")),
                        new OrderImport.Order(802, Arrays.asList("SOFA-1", "CUSHION-EXTRA-LARGE")),
                        new OrderImport.Order(803, Arrays.asList("RUG-4", null)),
                        new OrderImport.Order(804, Arrays.asList("SHELF-2")));
                System.out.println("imported: " + new OrderImport().importAll(importer, night));
                try (Statement st = warehouse.createStatement();
                     ResultSet rs = st.executeQuery("SELECT o.id, (SELECT COUNT(*) FROM order_line l WHERE l.order_id = o.id) FROM orders o ORDER BY o.id")) {
                    while (rs.next()) System.out.println("warehouse sees order " + rs.getInt(1) + " with " + rs.getInt(2) + " lines");
                }
                try (Statement st = warehouse.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM order_line")) {
                    rs.next();
                    System.out.println("lines in total: " + rs.getInt(1));
                }
                try (Statement st = warehouse.createStatement()) {
                    st.execute("SHUTDOWN");
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
