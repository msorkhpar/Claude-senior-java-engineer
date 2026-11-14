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
            String small = StockExport.csv(List.of(new StockExport.Item("BX-12", 30), new StockExport.Item("BX-13", 0)));
            System.out.println("small export:\n" + small);
            System.out.println("ends with a newline: " + small.endsWith("\n"));
            System.out.println("empty catalogue: " + StockExport.csv(List.of()));
            List<StockExport.Item> big = new ArrayList<>();
            for (int i = 0; i < 20_000; i++) big.add(new StockExport.Item("ST-" + i, i % 10));
            long start = System.nanoTime();
            String bigCsv = StockExport.csv(big);
            System.out.println("20000 items: " + bigCsv.lines().count() + " lines in "
                    + (System.nanoTime() - start) / 1_000_000 + " ms");
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
