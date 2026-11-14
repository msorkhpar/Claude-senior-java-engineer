package practice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
            List<String> closed = new ArrayList<>();
            Set<String> failing = Set.of("north/day-1", "south/day-1");
            PartitionWriters run = new PartitionWriters(partition -> new PartitionWriters.Writer() {
                @Override public void write(String row) { }

                @Override public void close() throws IOException {
                    closed.add(partition);
                    if (failing.contains(partition)) {
                        throw new IOException("flush of " + partition + " failed");
                    }
                }
            });
            run.write("north/day-1", "probe-2,4.5");
            run.write("east/day-1", "probe-8,6.25");
            run.write("north/day-1", "probe-3,4.0");
            run.write("south/day-1", "probe-11,9.75");
            try {
                run.close();
                System.out.println("run closed cleanly");
            } catch (IOException failed) {
                System.out.println("thrown: " + failed.getMessage());
                for (Throwable other : failed.getSuppressed()) {
                    System.out.println("suppressed: " + other.getMessage());
                }
            }
            System.out.println("closed, in order: " + closed);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
