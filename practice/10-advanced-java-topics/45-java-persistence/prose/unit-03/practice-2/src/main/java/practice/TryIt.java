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
            try (Connection job = h2("backoffice;DB_CLOSE_DELAY=-1"); Connection editor = h2("backoffice;LOCK_TIMEOUT=5000")) {
                try (Statement st = job.createStatement()) {
                    st.execute("CREATE TABLE product (id INT PRIMARY KEY, tags VARCHAR(500) NOT NULL, version INT NOT NULL)");
                    st.execute("INSERT INTO product VALUES (21, 'ceramic', 0), (22, 'glass', 4)");
                }
                System.out.println("plain add to 21: " + new ProductTags().addTag(editor, 21, "handmade"));
                int[] reads = {0};
                ProductTags slowEditor = new ProductTags(() -> {
                    if (++reads[0] == 1) {
                        try (Statement st = job.createStatement()) {
                            st.executeUpdate("UPDATE product SET tags = tags || ',winter', version = version + 1 WHERE id = 22");
                        }
                    }
                });
                System.out.println("editor's add to 22 while the job saves: " + slowEditor.addTag(editor, 22, "sale"));
                System.out.println("reads the editor needed: " + reads[0]);
                try (Statement st = job.createStatement(); ResultSet rs = st.executeQuery("SELECT id, tags, version FROM product ORDER BY id")) {
                    while (rs.next()) System.out.println("product " + rs.getInt(1) + ": " + rs.getString(2) + " (version " + rs.getInt(3) + ")");
                }
                try (Statement st = job.createStatement()) {
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
