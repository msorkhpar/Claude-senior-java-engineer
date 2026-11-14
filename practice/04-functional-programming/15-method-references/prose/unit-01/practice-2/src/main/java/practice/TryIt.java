package practice;

import java.util.function.Supplier;
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
            LimiterWiring.SettingsHolder holder = new LimiterWiring.SettingsHolder();
            LimiterWiring.RateLimiter limiter = LimiterWiring.limiter(holder);
            holder.reload(new LimiterWiring.Settings(2, "eu-west"));
            System.out.println("admitted of 6, limit 2: " + admitted(limiter, 6));
            holder.reload(new LimiterWiring.Settings(4, "eu-west"));
            limiter.nextWindow();
            System.out.println("admitted of 6 after reload to 4: " + admitted(limiter, 6));
            Supplier<String> region = LimiterWiring.exportHeaderRegion(holder);
            System.out.println("header of file 1: " + region.get());
            holder.reload(new LimiterWiring.Settings(4, "us-east"));
            System.out.println("header of file 2, after a reload: " + region.get());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static int admitted(LimiterWiring.RateLimiter limiter, int attempts) {
        int admitted = 0;
        for (int i = 0; i < attempts; i++) {
            if (limiter.tryAdmit()) admitted++;
        }
        return admitted;
    }
}
