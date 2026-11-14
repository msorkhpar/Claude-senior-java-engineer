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
            List<String> sent = new ArrayList<>();
            Payouts.Transfers bank = (account, cents) -> {
                sent.add(account + " " + cents);
                return "REF-" + (700 + sent.size());
            };
            List<String> trail = new ArrayList<>();
            Payouts.PayoutPolicy policy = new Payouts.PayoutPolicy(bank, trail::add);
            List<String> references = policy.pay(List.of(
                    new Payouts.Claim("C-301", "NL91-0417", 52_000, true),
                    new Payouts.Claim("C-302", "AT61-1904", 9_900, false),
                    new Payouts.Claim("C-303", "PL27-1140", 3_450, true)));
            System.out.println("references: " + references);
            System.out.println("sent to bank: " + sent);
            System.out.println("audit trail: " + trail);
            try {
                new Payouts.PayoutPolicy(bank, null);
                System.out.println("policy without audit: built");
            } catch (NullPointerException refused) {
                System.out.println("policy without audit: refused (" + refused.getMessage() + ")");
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
