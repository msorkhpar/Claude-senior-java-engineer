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
            try (Connection office = h2("wallets;DB_CLOSE_DELAY=-1"); Connection teller = h2("wallets;LOCK_TIMEOUT=5000")) {
                try (Statement st = office.createStatement()) {
                    st.execute("CREATE TABLE wallet (id INT PRIMARY KEY, balance INT NOT NULL CHECK (balance BETWEEN 0 AND 1000))");
                    st.execute("INSERT INTO wallet VALUES (31, 200), (32, 50), (33, 980)");
                }
                int[] debits = {0};
                Transfers chosenAsVictimOnce = new Transfers(() -> {
                    if (++debits[0] == 1) throw new SQLException("deadlock with the interest job (stand-in)", "40001");
                });
                chosenAsVictimOnce.transfer(teller, 31, 32, 70);
                System.out.println("31 -> 32, 70 after one 40001: debits tried " + debits[0]);
                debits[0] = 0;
                try {
                    new Transfers(() -> debits[0]++).transfer(teller, 31, 33, 60);
                } catch (SQLException refused) {
                    System.out.println("31 -> 33, 60 refused with SQLState " + refused.getSQLState() + " after " + debits[0] + " debit");
                }
                try (Statement st = office.createStatement(); ResultSet rs = st.executeQuery("SELECT id, balance FROM wallet ORDER BY id")) {
                    while (rs.next()) System.out.println("wallet " + rs.getInt(1) + ": " + rs.getInt(2));
                }
                try (Statement st = office.createStatement()) {
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
