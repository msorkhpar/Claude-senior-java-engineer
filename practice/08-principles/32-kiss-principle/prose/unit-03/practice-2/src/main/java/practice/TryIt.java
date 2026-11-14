package practice;

import java.util.ArrayList;
import java.util.List;
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
            Gate gate = new Gate();
            int[] ledgerLines = {0};
            System.out.println("job returns: " + gate.inside(() -> "batch 0912 settled"));
            RuntimeException[] failed = {null};
            List<Thread> jobs = new ArrayList<>();
            for (String region : List.of("settle-nordics", "settle-iberia", "settle-baltics")) {
                Thread job = new Thread(() -> {
                    try {
                        for (int i = 0; i < 3_000; i++) gate.inside(() -> ledgerLines[0]++);
                    } catch (RuntimeException e) {
                        failed[0] = e;
                    }
                }, region);
                jobs.add(job);
                job.start();
            }
            for (Thread job : jobs) job.join();
            if (failed[0] != null) throw failed[0];
            System.out.println("ledger lines from 3 jobs: " + gate.inside(() -> ledgerLines[0]));
            try {
                gate.inside(() -> { throw new IllegalArgumentException("ledger line 12 has no amount"); });
            } catch (IllegalArgumentException thrown) {
                System.out.println("failing job: " + thrown.getMessage());
            }
            String[] next = {null};
            Thread after = new Thread(() -> next[0] = gate.inside(() -> "batch 0913 settled"), "settle-adriatic");
            after.start();
            after.join(2_000);
            System.out.println("next job on another thread: " + (after.isAlive() ? "still waiting at the gate" : next[0]));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
