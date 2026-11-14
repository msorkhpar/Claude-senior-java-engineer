package practice;

import java.util.ArrayList;
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
            List<String> billed = new ArrayList<>();
            Map<String, Integer> scores = Map.of("kim", 35, "lou", 72, "max", 94);
            PaymentScreen screen = new PaymentScreen(card -> {
                billed.add(card.customer());
                return scores.get(card.customer());
            });
            System.out.println("transfer 250,000: " + screen.screen(new PaymentScreen.Transfer("nia", 250_000)));
            System.out.println("transfer 1,200,000: " + screen.screen(new PaymentScreen.Transfer("nia", 1_200_000)));
            System.out.println("transfer 6,000,000: " + screen.screen(new PaymentScreen.Transfer("oto", 6_000_000)));
            System.out.println("voucher: " + screen.screen(new PaymentScreen.Voucher("AUTUMN-10", 3_000)));
            System.out.println("card kim (35): " + screen.screen(new PaymentScreen.Card("kim", 4_200)));
            System.out.println("card lou (72): " + screen.screen(new PaymentScreen.Card("lou", 4_200)));
            System.out.println("card max (94): " + screen.screen(new PaymentScreen.Card("max", 4_200)));
            System.out.println("score calls billed: " + billed);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
