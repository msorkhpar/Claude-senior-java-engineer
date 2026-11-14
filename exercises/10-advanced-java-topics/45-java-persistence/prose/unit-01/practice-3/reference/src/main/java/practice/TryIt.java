package practice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal;
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
            try (Connection request = h2("wallets;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=2000"); Connection other = h2("wallets")) {
                try (Statement st = other.createStatement()) {
                    st.execute("CREATE TABLE wallet (id BIGINT PRIMARY KEY, balance DECIMAL(12,2) NOT NULL)");
                    st.execute("CREATE TABLE payment (wallet_id BIGINT NOT NULL, amount DECIMAL(12,2) NOT NULL, reference VARCHAR(16) NOT NULL)");
                    st.execute("INSERT INTO wallet VALUES (501, 300.00)");
                }
                Payments.pay(request, 501, new BigDecimal("42.10"), "cart-901");
                System.out.println("after cart-901: balance " + query(other, "SELECT balance FROM wallet WHERE id = 501")
                        + ", payments " + query(other, "SELECT COUNT(*) FROM payment"));
                String[][] failing = {{"501", "15.00", "subscription-2026-0042"}, {"777", "5.00", "cart-902"}};
                for (String[] f : failing) {
                    try {
                        Payments.pay(request, Long.parseLong(f[0]), new BigDecimal(f[1]), f[2]);
                    } catch (SQLException refused) {
                        System.out.println(f[2] + " on wallet " + f[0] + " refused");
                    }
                }
                System.out.println("after the failures: balance " + query(other, "SELECT balance FROM wallet WHERE id = 501")
                        + ", payments " + query(other, "SELECT COUNT(*) FROM payment"));
                System.out.println("request connection autocommit: " + request.getAutoCommit());
                try (Statement st = other.createStatement()) {
                    st.execute("SHUTDOWN");
                }
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static String query(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getString(1);
        }
    }

    /** The practice's database, H2 in memory: Run sees the tests' libraries too. */
    private static Connection h2(String name) throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:" + name);
    }
}
