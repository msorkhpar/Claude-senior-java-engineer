package practice;

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
            EncoderSessions gpu = new EncoderSessions(6);
            System.out.println("4K job, tryReserve(4): " + gpu.tryReserve(4));
            System.out.println("free after it: " + gpu.free());
            System.out.println("another 4K job, tryReserve(4): " + gpu.tryReserve(4));
            System.out.println("free after the refusal: " + gpu.free());
            Thread job = new Thread(() -> {
                try {
                    gpu.reserve(3);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            job.setDaemon(true);
            job.start();
            long until = System.nanoTime() + 2_000_000_000L;
            while (gpu.waitingJobs() == 0 && job.isAlive() && System.nanoTime() < until) Thread.onSpinWait();
            System.out.println("jobs waiting for 3 sessions: " + gpu.waitingJobs());
            gpu.giveBack(4);
            job.join(3_000);
            System.out.println("waiting job got its sessions: " + !job.isAlive());
            System.out.println("free at the end: " + gpu.free());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
