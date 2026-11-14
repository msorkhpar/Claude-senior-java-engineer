package practice;

import java.util.function.Function;
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
            Function<String, String> june = Ledger.withPeriod("/2032-06");
            System.out.println("export id: " + june.apply("JE-77"));
            Ledger.FlaggedFormat format = new Ledger.FlaggedFormat(500_000);
            System.out.println("export column: " + format.exportColumn().apply(812_345));
            System.out.println("screen column: " + format.screenColumn().apply(812_345));
            System.out.println("screen column, small amount: " + format.screenColumn().apply(1_299));
            Ledger.FlaggedFormat starred = new Ledger.FlaggedFormat(500_000) {
                @Override
                public String render(long cents) {
                    return "* " + super.render(cents);
                }
            };
            System.out.println("a team's own screen: " + starred.screenColumn().apply(812_345));
            System.out.println("a team's own export: " + starred.exportColumn().apply(812_345));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
