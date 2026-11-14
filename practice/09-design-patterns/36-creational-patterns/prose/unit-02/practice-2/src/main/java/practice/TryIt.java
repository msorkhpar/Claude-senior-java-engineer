package practice;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
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
            RequestPool pool = new RequestPool(2, 4);
            try {
                RequestPool.REQUEST_ID.set("inv-2208");
                Future<String> callable = pool.submit(() -> "callable ran under " + RequestPool.REQUEST_ID.get());
                String[] seen = {null};
                Future<?> runnable = pool.submit(() -> { seen[0] = RequestPool.REQUEST_ID.get(); });
                RequestPool.REQUEST_ID.remove();
                System.out.println(callable.get());
                runnable.get();
                System.out.println("runnable ran under " + seen[0]);
                CompletableFuture<String> housekeeping = new CompletableFuture<>();
                pool.execute(() -> housekeeping.complete(String.valueOf(RequestPool.REQUEST_ID.get())));
                System.out.println("housekeeping ran under " + housekeeping.get());
                RequestPool.REQUEST_ID.set("inv-2209");
                System.out.println("submitted without id: " + pool.submit(() -> "id " + RequestPool.REQUEST_ID.get()).get());
                System.out.println("handler keeps: " + RequestPool.REQUEST_ID.get());
            } finally {
                pool.shutdownNow();
                RequestPool.REQUEST_ID.remove();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
