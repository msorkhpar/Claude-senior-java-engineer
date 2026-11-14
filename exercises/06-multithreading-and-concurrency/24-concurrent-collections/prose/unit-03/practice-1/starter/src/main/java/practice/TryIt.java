package practice;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
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
            DueTasks tick = new DueTasks();
            for (String id : new String[] {"job-61", "job-62", "job-63", "job-64", "job-65"}) tick.add(id);
            StringBuilder visited = new StringBuilder();
            Iterator<String> walk = tick.iterator();
            while (walk.hasNext()) {
                String id = walk.next();
                visited.append(id).append(' ');
                if (id.equals("job-62") || id.equals("job-63")) walk.remove();
            }
            System.out.println("visited: " + visited.toString().trim());
            System.out.println("left after the walk: " + tick.toList());
            Iterator<String> careless = tick.iterator();
            careless.next();
            tick.add("job-66");
            try {
                System.out.println("next after an add behind the walk: " + careless.next());
            } catch (ConcurrentModificationException refused) {
                System.out.println("next after an add behind the walk: " + refused.getClass().getSimpleName());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
