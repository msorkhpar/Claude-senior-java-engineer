package practice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
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
            List<Integer> buffer = new ArrayList<>(IntStream.range(0, 25).map(i -> 100 + i).boxed().toList());
            List<Integer> replayed = new LinkedList<>(buffer);
            System.out.println("buffer, every 6th: " + new Downsampler(6).sample(buffer));
            System.out.println("replayed, every 6th: " + new Downsampler(6).sample(replayed));
            System.out.println("replayed, every 10th: " + new Downsampler(10).sample(replayed));
            List<Integer> big = new LinkedList<>(IntStream.range(0, 200_000).boxed().toList());
            long started = System.nanoTime();
            List<Integer> kept = new Downsampler(7).sample(big);
            System.out.println("200,000 linked readings, every 7th: " + kept.size() + " kept, last " + kept.get(kept.size() - 1));
            System.out.println("took under a second: " + (System.nanoTime() - started < 1_000_000_000L));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
