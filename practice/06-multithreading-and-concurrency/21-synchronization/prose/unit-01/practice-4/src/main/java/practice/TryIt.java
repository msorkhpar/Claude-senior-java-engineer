package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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
            CommitSequencer sequencer = new CommitSequencer(101);
            List<String> commits = Collections.synchronizedList(new ArrayList<>());
            List<Thread> workers = new ArrayList<>();
            AtomicReference<RuntimeException> failure = new AtomicReference<>();
            for (long batch : new long[] {104, 102, 103, 101}) {
                Thread worker = new Thread(() -> {
                    try {
                        sequencer.awaitTurn(batch);
                        commits.add("batch " + batch);
                        sequencer.done(batch);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (RuntimeException e) {
                        failure.set(e);
                    }
                }, "batch-" + batch);
                worker.setDaemon(true);
                workers.add(worker);
                worker.start();
                Thread.sleep(50);
            }
            for (Thread worker : workers) worker.join(3_000);
            if (failure.get() != null) throw failure.get();
            System.out.println("workers started waiting in order: 104, 102, 103, 101");
            System.out.println("commit order: " + commits);
            System.out.println("next batch: " + sequencer.next());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
