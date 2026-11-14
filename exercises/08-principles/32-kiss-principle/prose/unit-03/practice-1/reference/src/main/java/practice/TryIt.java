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
            Wallet wallet = new Wallet(20_000, 150, () -> { });
            System.out.println("opening: " + wallet.statement() + ", total " + wallet.total());
            wallet.transfer(1_000);
            System.out.println("after moving 1000: " + wallet.statement());
            RuntimeException[] failed = {null};
            List<Thread> savers = new ArrayList<>();
            for (int t = 0; t < 3; t++) {
                Thread saver = new Thread(() -> {
                    try {
                        for (int i = 0; i < 1_000; i++) wallet.transfer(5);
                    } catch (RuntimeException e) {
                        failed[0] = e;
                    }
                });
                savers.add(saver);
                saver.start();
            }
            for (Thread saver : savers) saver.join();
            if (failed[0] != null) throw failed[0];
            System.out.println("after 3 threads x 1000 x 5: " + wallet.statement());
            System.out.println("total: " + wallet.total());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
