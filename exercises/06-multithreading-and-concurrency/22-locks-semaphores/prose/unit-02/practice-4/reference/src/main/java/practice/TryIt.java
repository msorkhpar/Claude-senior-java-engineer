package practice;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            ExecutorService replicaThreads = Executors.newCachedThreadPool();
            try {
                QuorumWriter.Replica atOnce = (version, record, ack) -> ack.run();
                QuorumWriter.Replica ownThread = (version, record, ack) -> replicaThreads.execute(ack);
                QuorumWriter.Replica down = (version, record, ack) -> { };
                QuorumWriter healthy = new QuorumWriter(List.of(ownThread, atOnce, down), 2, 2_000);
                System.out.println("write booking-310:held: " + healthy.write("booking-310:held"));
                System.out.println("write booking-310:confirmed: " + healthy.write("booking-310:confirmed"));
                QuorumWriter degraded = new QuorumWriter(List.of(atOnce, ownThread, down, down), 3, 150);
                System.out.println("write with 2 of 4 up, quorum 3: " + degraded.write("booking-311:held"));
            } finally {
                replicaThreads.shutdownNow();
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
