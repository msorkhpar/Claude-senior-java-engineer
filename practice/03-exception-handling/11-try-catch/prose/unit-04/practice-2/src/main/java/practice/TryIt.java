package practice;

import java.io.IOException;
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
            ArchiveReader.Pool pool = new ArchiveReader.Pool(1, "ARC1\nmeter-7710,2027-05-01,64.2\nmeter-7710,2027-05-02,66.9\n");
            for (int i = 1; i <= 3; i++) {
                System.out.println("read " + i + ": " + ArchiveReader.firstRecord(pool));
            }
            ArchiveReader.Pool csvPool = new ArchiveReader.Pool(1, "meter,day,kwh\nmeter-7710,2027-05-01,64.2\n");
            for (int i = 1; i <= 2; i++) {
                try {
                    ArchiveReader.firstRecord(csvPool);
                } catch (IOException rejected) {
                    System.out.println("csv upload " + i + ": " + rejected.getMessage() + ", idle connections: " + csvPool.idle().size());
                }
            }
            ArchiveReader.Pool two = new ArchiveReader.Pool(2, "ARC1\nmeter-1,2027-05-01,1.0\n");
            ArchiveReader.firstRecord(two);
            System.out.println("two borrows get different connections: " + (two.borrow() != two.borrow()));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
