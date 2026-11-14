package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
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
            List<String> crafted = new ArrayList<>(List.of("X-Z-"));
            for (int i = 0; i < 10; i++) {
                List<String> longer = new ArrayList<>();
                for (String n : crafted) { longer.add(n + "Ea"); longer.add(n + "FB"); }
                crafted = longer;
            }
            Map<HeaderName, String> headers = new HashMap<>();
            for (String name : crafted) headers.put(new HeaderName(name), "junk");
            headers.put(new HeaderName("Content-Type"), "application/json");
            System.out.println("names sharing one hash code: " + crafted.size());
            HeaderName.EQUALS_CALLS.set(0);
            for (String name : crafted) headers.get(new HeaderName(new String(name)));
            System.out.println("equals calls per lookup: " + HeaderName.EQUALS_CALLS.get() / crafted.size());
            System.out.println("Content-Type: " + headers.get(new HeaderName("Content-Type")));
            TreeSet<HeaderName> log = new TreeSet<>();
            for (String name : List.of("User-Agent", "Accept", "accept", "Authorization")) log.add(new HeaderName(name));
            System.out.println("debug log: " + log);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
