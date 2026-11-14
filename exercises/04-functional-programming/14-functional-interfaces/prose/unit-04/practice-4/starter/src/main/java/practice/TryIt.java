package practice;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
            List<IssueSearch.Issue> board = List.of(
                    new IssueSearch.Issue(1, "Ana", IssueSearch.Priority.URGENT, Set.of("api")),
                    new IssueSearch.Issue(2, "Ben", IssueSearch.Priority.LOW, Set.of("ui", "api")),
                    new IssueSearch.Issue(3, "Ana", IssueSearch.Priority.MEDIUM, Set.of("ui")),
                    new IssueSearch.Issue(4, "Cy", IssueSearch.Priority.HIGH, Set.of()));
            System.out.println("blank form: " + ids(board, Optional.empty(), Optional.empty(), Set.of()));
            System.out.println("Ana: " + ids(board, Optional.of("Ana"), Optional.empty(), Set.of()));
            System.out.println("HIGH or more: " + ids(board, Optional.empty(), Optional.of(IssueSearch.Priority.HIGH), Set.of()));
            System.out.println("labels ui or api: " + ids(board, Optional.empty(), Optional.empty(), Set.of("ui", "api")));
            System.out.println("Ana, label ui: " + ids(board, Optional.of("Ana"), Optional.empty(), Set.of("ui")));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static List<Integer> ids(List<IssueSearch.Issue> board, Optional<String> assignee,
                                     Optional<IssueSearch.Priority> atLeast, Set<String> labels) {
        return IssueSearch.search(board, new IssueSearch.Form(assignee, atLeast, labels)).stream()
                .map(IssueSearch.Issue::id).toList();
    }
}
