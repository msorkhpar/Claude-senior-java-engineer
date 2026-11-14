package practice;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
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
            ForkJoinPool servicePool = new ForkJoinPool(2);
            try {
                PickLists pickLists = new PickLists(servicePool);
                System.out.println("lists: " + pickLists.buildAll(List.of(
                        warehouse(() -> "Hamburg: 23 lines"), warehouse(() -> "Brno: 4 lines"), warehouse(() -> "Bilbao: 16 lines"))));
                CountDownLatch rigaStarted = new CountDownLatch(1);
                CountDownLatch tallinnStarted = new CountDownLatch(1);
                System.out.println("cross-dock: " + pickLists.buildAll(List.of(
                        warehouse(() -> crossDock("Riga", rigaStarted, tallinnStarted)),
                        warehouse(() -> crossDock("Tallinn", tallinnStarted, rigaStarted)))));
                try {
                    pickLists.buildAll(List.of(warehouse(() -> "Bari: 2 lines"),
                            warehouse(() -> { throw new IllegalArgumentException("Napoli: no stock table"); })));
                } catch (IllegalArgumentException failed) {
                    System.out.println("run failed with: " + failed.getClass().getSimpleName());
                }
            } finally {
                servicePool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static ForkJoinTask<String> warehouse(Supplier<String> build) {
        return ForkJoinTask.adapt(build::get);
    }

    /** A warehouse sharing a cross-dock: it starts, then waits up to 3 s for its partner to start. */
    static String crossDock(String name, CountDownLatch mine, CountDownLatch partners) {
        mine.countDown();
        try {
            return partners.await(3, TimeUnit.SECONDS) ? name + ": cross-dock list" : name + ": partner never started";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return name + ": interrupted";
        }
    }
}
