package practice;

import java.io.IOException;
import java.net.SocketTimeoutException;
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
            Retrying.Rates steady = Retrying.wrap(new Scripted(), Retrying.Rates.class, 3);
            System.out.println("USD/JPY at once: " + steady.rate("USD/JPY"));
            Scripted flaky = new Scripted(new SocketTimeoutException("read timed out"), new IOException("connection reset"));
            System.out.println("USD/JPY after two drops: " + Retrying.wrap(flaky, Retrying.Rates.class, 3).rate("USD/JPY")
                    + " (" + flaky.calls + " calls)");
            Scripted down = new Scripted(new IOException("connection reset"), new IOException("connection reset"),
                    new SocketTimeoutException("read timed out"));
            try {
                Retrying.wrap(down, Retrying.Rates.class, 3).rate("USD/JPY");
            } catch (IOException e) {
                System.out.println("all attempts failed: " + e + " (" + down.calls + " calls)");
            }
            Scripted rejecting = new Scripted(new IllegalArgumentException("unknown pair XAG/USD"));
            try {
                Retrying.wrap(rejecting, Retrying.Rates.class, 3).rate("XAG/USD");
            } catch (IllegalArgumentException e) {
                System.out.println("rejected pair: " + e.getMessage() + " (" + rejecting.calls + " call)");
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A rate service that fails with the given exceptions, in order, and then answers. */
    static final class Scripted implements Retrying.Rates {
        private final java.util.ArrayDeque<Exception> failures;
        int calls;
        Scripted(Exception... failures) { this.failures = new java.util.ArrayDeque<>(java.util.List.of(failures)); }
        @Override
        public long rate(String pair) throws IOException {
            calls++;
            Exception next = failures.poll();
            if (next instanceof IOException io) throw io;
            if (next != null) throw (RuntimeException) next;
            return 1_563_250;
        }
    }
}
