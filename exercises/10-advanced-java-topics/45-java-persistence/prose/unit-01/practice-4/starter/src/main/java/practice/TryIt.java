package practice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            try (Connection agent = h2("helpdesk;DB_CLOSE_DELAY=-1"); Connection colleague = h2("helpdesk")) {
                try (Statement st = colleague.createStatement()) {
                    st.execute("CREATE TABLE ticket (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, title VARCHAR(80) NOT NULL)");
                    st.execute("CREATE TABLE ticket_tag (ticket_id BIGINT NOT NULL REFERENCES ticket (id), tag VARCHAR(20) NOT NULL)");
                    st.execute("ALTER TABLE ticket ALTER COLUMN id RESTART WITH 4100");
                    st.execute("INSERT INTO ticket (title) VALUES ('password reset')");
                }
                long first = Tickets.open(agent, "screen cracked", List.of("hardware", "laptop"));
                System.out.println("screen cracked: id " + first);
                try (Statement st = colleague.createStatement()) {
                    st.execute("INSERT INTO ticket (title) VALUES ('wifi drops')");
                }
                long second = Tickets.open(agent, "password reset", List.of("account"));
                System.out.println("password reset: id " + second);
                try (Statement st = colleague.createStatement();
                     ResultSet rs = st.executeQuery("SELECT t.id, t.title, GROUP_CONCAT(g.tag ORDER BY g.tag) FROM ticket t "
                             + "LEFT JOIN ticket_tag g ON g.ticket_id = t.id GROUP BY t.id, t.title ORDER BY t.id")) {
                    while (rs.next()) System.out.println("ticket " + rs.getLong(1) + " " + rs.getString(2) + ": " + rs.getString(3));
                }
                try (Statement st = colleague.createStatement()) {
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
