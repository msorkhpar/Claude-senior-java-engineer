package practice;

import java.lang.ref.WeakReference;
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
            UploadQueue queue = new UploadQueue();
            for (int i = 1; i <= 10; i++) queue.put(new UploadQueue.Upload("scan-" + i + ".pdf", new byte[512 * 1024]));
            System.out.println("waiting: " + queue.size());
            System.out.println("scanner takes: " + queue.take().name());
            System.out.println("scanner takes: " + queue.take().name());
            System.out.println("waiting: " + queue.size());
            WeakReference<byte[]> scanned = scannedBody(queue);
            queue.clear();
            System.out.println("after clear, waiting: " + queue.size() + ", take gives: " + queue.take());
            for (int i = 0; i < 10 && scanned.get() != null; i++) System.gc();
            System.out.println("scanned body still in memory: " + (scanned.get() != null));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static WeakReference<byte[]> scannedBody(UploadQueue queue) {
        return new WeakReference<>(queue.take().body());
    }
}
