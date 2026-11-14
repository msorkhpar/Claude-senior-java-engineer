package practice;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
            ForkJoinPool renderPool = new ForkJoinPool(1);
            try {
                LicenceSeats busy = new LicenceSeats(0);
                Future<String> render = renderPool.submit(() -> {
                    busy.take();
                    return "preview-881 rendered";
                });
                renderPool.submit(busy::giveBack);
                System.out.println("render on a full licence: " + finish(render));
                System.out.println("free seats after it: " + busy.free());
                LicenceSeats quiet = new LicenceSeats(4);
                System.out.println("render on a quiet day: " + finish(renderPool.submit(() -> {
                    quiet.take();
                    return "preview-882 rendered";
                })));
                System.out.println("free seats of 4: " + quiet.free());
            } finally {
                renderPool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** Waits up to 3 s for a render; passes on what it threw, so Run shows it. */
    static String finish(Future<String> render) throws Exception {
        try {
            return render.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return "still waiting after 3 s";
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException stop) throw stop;
            throw e;
        }
    }
}
