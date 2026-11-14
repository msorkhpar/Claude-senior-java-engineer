package practice;

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
            Permits permits = new Permits();
            permits.register("LO-515-K", "TW3108");
            permits.register("L0-515-K", "TW3109");
            permits.register("NE-40-V", "HB7120");
            System.out.println("code tw 31O8: " + permits.carWithPermit("tw 31O8"));
            System.out.println("code TW-3IO9: " + permits.carWithPermit("TW-3IO9"));
            System.out.println("code hb712o: " + permits.carWithPermit("hb712o"));
            permits.transfer("ne 40 v", "ZA-8-M");
            System.out.println("code HB7120 after transfer: " + permits.carWithPermit("HB7120"));
            System.out.println("code QQ0000: " + permits.carWithPermit("QQ0000"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
