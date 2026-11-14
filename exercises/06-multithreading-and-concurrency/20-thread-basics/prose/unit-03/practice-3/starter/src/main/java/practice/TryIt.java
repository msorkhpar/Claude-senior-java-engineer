package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
            ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "imports");
                t.setDaemon(true);
                return t;
            });
            CountDownLatch waitingOnStorage = new CountDownLatch(1);
            CountDownLatch storageAnswers = new CountDownLatch(1);
            try {
                Recorder store = new Recorder();
                CsvImport imports = new CsvImport(pool, chunk -> {
                    if (chunk.equals("kai;lou")) {
                        waitingOnStorage.countDown();
                        storageAnswers.await();
                    }
                    return Arrays.asList(chunk.split(";"));
                }, store);
                try {
                    imports.start("imp-100", List.of("ida;jon", "max")).get(5, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause() instanceof RuntimeException r ? r : new IllegalStateException(e.getCause());
                }
                System.out.println("imp-100 staged " + store.staged + ", committed " + store.committed);
                store.staged.clear();
                Future<?> running = imports.start("imp-200", List.of("ivy", "kai;lou", "mo"));
                waitingOnStorage.await(5, TimeUnit.SECONDS);
                System.out.println("cancel imp-200 while it waits on storage: " + imports.cancel(running));
                pool.shutdown();
                pool.awaitTermination(5, TimeUnit.SECONDS);
                System.out.println("imp-200 staged " + store.staged + ", committed " + store.committed);
            } finally {
                storageAnswers.countDown();
                pool.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static final class Recorder implements CsvImport.Store {
        final List<String> staged = Collections.synchronizedList(new ArrayList<>());
        final List<String> committed = Collections.synchronizedList(new ArrayList<>());
        @Override public void stage(String importId, List<String> rows) { staged.addAll(rows); }
        @Override public void commit(String importId) { committed.add(importId); }
    }
}
