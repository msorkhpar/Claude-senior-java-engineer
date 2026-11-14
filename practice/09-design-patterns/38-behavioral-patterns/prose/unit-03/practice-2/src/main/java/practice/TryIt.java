package practice;

import java.util.concurrent.TimeoutException;
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
            Refunds.RefundService service = new Refunds.RefundService();
            int[] sends = {0};
            Refunds.Link flaky = command -> {
                String reply = service.handle(command);
                if (++sends[0] == 1) throw new TimeoutException("reply lost");
                return reply;
            };
            int[] next = {0};
            Refunds.RefundClient client = new Refunds.RefundClient(flaky, () -> "key-" + (++next[0]), 3);
            System.out.println("refund o-2201 (first reply lost): " + client.refund("o-2201", 3_450));
            System.out.println("refund o-2202: " + client.refund("o-2202", 990));
            System.out.println("refund o-2202 again, same amount: " + client.refund("o-2202", 990));
            System.out.println("link sends: " + sends[0]);
            for (Refunds.Payout p : service.payouts()) System.out.println("paid: " + p);
            Refunds.RefundClient silent = new Refunds.RefundClient(c -> { throw new TimeoutException("no reply"); }, () -> "key-x", 2);
            try {
                silent.refund("o-2299", 100);
            } catch (Refunds.NoReplyException e) {
                System.out.println("silent link: " + e.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
