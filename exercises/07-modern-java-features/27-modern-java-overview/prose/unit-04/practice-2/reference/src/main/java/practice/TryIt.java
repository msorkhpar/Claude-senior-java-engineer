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
            ChaosRun recorded = new ChaosRun("L64X128MixRandom", 2_026L);
            List<List<Integer>> seen = new ArrayList<>();
            for (int worker = 0; worker < 3; worker++) seen.add(recorded.failingCalls(worker, 20, 25));
            for (int worker = 0; worker < 3; worker++) System.out.println("worker " + worker + " fails calls " + seen.get(worker));
            ChaosRun replay = new ChaosRun("L64X128MixRandom", 2_026L);
            boolean same = true;
            for (int worker = 2; worker >= 0; worker--) same &= replay.failingCalls(worker, 20, 25).equals(seen.get(worker));
            System.out.println("replay, workers started in reverse, fails the same calls: " + same);
            System.out.println("generator of a run named Random: " + new ChaosRun("Random", 9L).generatorFor(0).getClass().getSimpleName());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
