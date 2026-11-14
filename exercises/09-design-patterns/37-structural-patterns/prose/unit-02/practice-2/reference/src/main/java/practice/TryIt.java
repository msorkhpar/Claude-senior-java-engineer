package practice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            int[] requests = {0};
            QuoteMetrics.QuoteSource remote = new QuoteMetrics.QuoteSource() {
                @Override public QuoteMetrics.Quote quote(String symbol) {
                    requests[0]++;
                    return new QuoteMetrics.Quote(symbol, 1_000L + symbol.length());
                }
                @Override public Map<String, QuoteMetrics.Quote> quoteAll(List<String> symbols) {
                    requests[0]++;
                    Map<String, QuoteMetrics.Quote> page = new LinkedHashMap<>();
                    for (String s : symbols) page.put(s, new QuoteMetrics.Quote(s, 1_000L + s.length()));
                    return page;
                }
            };
            QuoteMetrics.Metered metered = new QuoteMetrics.Metered(remote);
            System.out.println("page: " + metered.quoteAll(List.of("ABB", "ERIC-B", "NESN", "VOLV-B")).keySet());
            metered.quote("ROG");
            metered.quoteAll(List.of("UBSG", "ZURN"));
            System.out.println("symbols quoted: " + metered.symbolsQuoted());
            System.out.println("remote requests: " + requests[0]);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
