package practice;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
            List<Ledger.Line> month = List.of(
                    new Ledger.Line("DE89-3704", 2_500L, 501L),
                    new Ledger.Line("FR76-3000", -1_200L, 502L),
                    new Ledger.Line("DE89-3704", 7_800L, 503L),
                    new Ledger.Line("FR76-3000", 450L, 504L),
                    new Ledger.Line("DE89-3704", -300L, 505L));
            Map<String, List<Ledger.Line>> statements = new TreeMap<>(Ledger.statements(month.parallelStream()));
            statements.forEach((account, lines) -> System.out.println("statement " + account + ": "
                    + lines.stream().map(Ledger.Line::postingNo).toList()));
            Map<String, Ledger.Line> opening = new TreeMap<>(Ledger.openingLines(month.parallelStream()));
            opening.forEach((account, line) -> System.out.println("opening " + account + ": posting "
                    + line.postingNo() + ", " + line.cents() + " cents"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
