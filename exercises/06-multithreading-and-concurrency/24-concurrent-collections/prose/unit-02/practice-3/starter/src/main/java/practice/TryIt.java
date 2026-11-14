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
            WordIndex index = new WordIndex();
            index.add("harbour", "doc-301");
            index.add("harbour", "doc-302");
            index.add("lighthouse", "doc-302");
            index.add("harbour", "doc-301");
            System.out.println("harbour: " + index.documentsWith("harbour").stream().sorted().toList());
            List<String> walked = new ArrayList<>();
            for (String doc : index.documentsWith("harbour")) {
                walked.add(doc);
                index.add("harbour", "doc-303");
            }
            System.out.println("walk saw doc-301 and doc-302: " + walked.containsAll(List.of("doc-301", "doc-302")));
            index.remove("lighthouse", "doc-302");
            System.out.println("lighthouse still known: " + index.knows("lighthouse"));
            index.remove("harbour", "doc-301");
            System.out.println("harbour after removing doc-301: " + index.documentsWith("harbour").stream().sorted().toList());
            index.remove("anchor", "doc-301");
            System.out.println("anchor known: " + index.knows("anchor"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
