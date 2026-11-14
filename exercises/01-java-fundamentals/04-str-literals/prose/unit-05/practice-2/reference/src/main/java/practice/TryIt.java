package practice;

import java.util.ArrayList;
import java.util.List;
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
            ScratchBuffer buffer = new ScratchBuffer();
            System.out.print(buffer.format(List.of("INVOICE HARBOR", "00001;ROPE;3.20")));
            System.out.print(buffer.format(List.of("INVOICE MERIDIAN", "00001;SAIL;80.00")));
            System.out.println("capacity after ordinary invoices: " + buffer.capacity());
            List<String> huge = new ArrayList<>();
            for (int i = 1; i <= 15_000; i++) huge.add("%05d;CLEAT-%d;1.%02d".formatted(i, i % 9, i % 100));
            System.out.println("huge invoice length: " + buffer.format(huge).length());
            buffer.format(List.of("INVOICE PIER-9", "00001;BUOY;45.00"));
            System.out.println("capacity after the next ordinary invoice: " + buffer.capacity());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
