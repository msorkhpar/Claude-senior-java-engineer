package practice;

import java.util.concurrent.Semaphore;
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
            Semaphore renderers = new Semaphore(2, true);
            RenderAdmission admission = new RenderAdmission(renderers, 200);
            System.out.println("preview with free renderers: " + admission.preview(() -> { }));
            System.out.println("batch with free renderers: " + admission.batch(() -> { }));
            renderers.acquire(2);
            try {
                long started = System.nanoTime();
                boolean batch = admission.batch(() -> { });
                long batchMs = (System.nanoTime() - started) / 1_000_000;
                System.out.println("batch while both renderers are busy: " + batch + ", answered at once: " + (batchMs < 100));
                started = System.nanoTime();
                boolean preview = admission.preview(() -> { });
                long previewMs = (System.nanoTime() - started) / 1_000_000;
                System.out.println("preview while both are busy: " + preview + ", waited about 200 ms: " + (previewMs >= 190 && previewMs < 1_000));
            } finally {
                renderers.release(2);
            }
            System.out.println("renderers free at the end: " + renderers.availablePermits());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
