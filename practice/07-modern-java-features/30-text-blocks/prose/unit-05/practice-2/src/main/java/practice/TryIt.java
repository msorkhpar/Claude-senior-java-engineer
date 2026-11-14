package practice;

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
            String expected = "order,item,qty\nA-77,bolts,500\nA-78,nuts,250\n";
            String fromWindows = "order,item,qty\r\nA-77,bolts,500\r\nA-78,nuts,250";
            System.out.println("lines of the Windows export: " + ExportCheck.linesOf(fromWindows));
            System.out.println("Windows export: " + ExportCheck.verdict(expected, fromWindows));
            System.out.println("changed quantity: " + ExportCheck.verdict(expected, expected.replace("500", "499")));
            System.out.println("trailing space: " + ExportCheck.verdict(expected, expected.replace("nuts,250", "nuts,250 ")));
            System.out.println("missing last row: " + ExportCheck.verdict(expected, "order,item,qty\nA-77,bolts,500\n"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
