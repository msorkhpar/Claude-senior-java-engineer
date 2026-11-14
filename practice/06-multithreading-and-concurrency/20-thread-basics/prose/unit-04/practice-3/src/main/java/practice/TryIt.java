package practice;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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
            BlockingQueue<String> indexed = new LinkedBlockingQueue<>();
            IndexWorker worker = new IndexWorker(() -> {
                Thread.sleep(300);
                return Map.of("sofa", "couch", "cellphone", "mobile");
            }, indexed::add);
            try {
                long started = System.nanoTime();
                worker.start();
                long tookMs = (System.nanoTime() - started) / 1_000_000;
                System.out.println("table loaded when start() returned: " + worker.loaded());
                System.out.println("start() waited for the 300 ms load: " + (tookMs >= 290));
                worker.submit("sofa");
                worker.submit("lamp");
                worker.submit("cellphone");
                for (int i = 0; i < 3; i++) System.out.println("indexed: " + indexed.poll(2, TimeUnit.SECONDS));
            } finally {
                worker.stop();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
