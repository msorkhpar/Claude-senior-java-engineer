package practice;

import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) {
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
            Properties full = new Properties();
            full.setProperty("export.dir", "/data/exports/nightly");
            full.setProperty("export.batch.size", "40");
            ExportSettings s = ExportSettings.from(full);
            System.out.println("dir: " + s.dir + ", batch size: " + s.batchSize + ", retries: " + s.retries);
            Properties[] broken = {new Properties(), new Properties(), new Properties()};
            broken[0].setProperty("export.dir", "/data/exports/nightly");
            broken[0].setProperty("export.batchsize", "40");
            broken[1].setProperty("export.retries", "4");
            broken[2].setProperty("export.dir", "/data/exports/nightly");
            broken[2].setProperty("export.batch.size", "forty");
            for (Properties file : broken) {
                try {
                    ExportSettings.from(file);
                    System.out.println("started with " + file);
                } catch (IllegalArgumentException refused) {
                    System.out.println("refused: " + refused.getMessage());
                }
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
