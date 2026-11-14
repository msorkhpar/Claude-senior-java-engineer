package practice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
            try (Connection db = h2("meters")) {
                try (Statement st = db.createStatement()) {
                    st.execute("CREATE TABLE meter_reading (meter VARCHAR(40) NOT NULL, wh BIGINT NOT NULL)");
                }
                List<MeterImport.Reading> night = new ArrayList<>();
                for (int i = 0; i < 9; i++) night.add(new MeterImport.Reading("grid-2/m" + (500 + i), 7_000L + 21L * i));
                night.add(new MeterImport.Reading("D'Arcy Rd 1", 6_480));
                System.out.println("rows reported, chunks of 4: " + MeterImport.importAll(db, night, 4));
                System.out.println("empty night: " + MeterImport.importAll(db, List.of(), 50));
                try (Statement st = db.createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*), SUM(wh), MIN(meter) FROM meter_reading")) {
                    rs.next();
                    System.out.println("rows stored: " + rs.getInt(1) + ", total wh: " + rs.getLong(2) + ", first meter: " + rs.getString(3));
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
