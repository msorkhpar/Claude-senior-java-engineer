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
            List<Folders.Message> inbox = new ArrayList<>(List.of(
                    new Folders.Message("team@example.org", "Standup notes"),
                    new Folders.Message("hr@example.org", "Holiday calendar")));
            List<Folders.Message> archive = new ArrayList<>(List.of(new Folders.Message("bank@example.org", "Statement")));
            System.out.println("moved: " + Folders.archiveAll(inbox, archive));
            System.out.println("inbox screen: " + subjects(inbox));
            System.out.println("archive screen: " + subjects(archive));
            List<Folders.Message> emptyArchive = new ArrayList<>();
            List<Folders.Message> secondInbox = new ArrayList<>(List.of(new Folders.Message("it@example.org", "Password expiry")));
            System.out.println("moved into empty archive: " + Folders.archiveAll(secondInbox, emptyArchive) + ", archive now " + emptyArchive.size());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** The subjects a folder screen shows, in order. */
    private static List<String> subjects(List<Folders.Message> folder) {
        return folder.stream().map(Folders.Message::subject).toList();
    }
}
