package practice;

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
            Workshops workshops = new Workshops((workshop, enrolledSoFar) -> enrolledSoFar < 2);
            Workshops.SignupPage page = new Workshops.SignupPage(workshops);
            Workshops.BadgeImport badges = new Workshops.BadgeImport(workshops);
            System.out.println("badge B-3301: " + badges.importBadge("records-and-sealed", "B-3301"));
            System.out.println("sign-up ivy@example.com: " + page.signUp("records-and-sealed", "ivy@example.com"));
            System.out.println("badge B-3302: " + badges.importBadge("records-and-sealed", "B-3302"));
            System.out.println("sign-up jo@example.com: " + page.signUp("records-and-sealed", "jo@example.com"));
            workshops.addWalkIn("records-and-sealed");
            System.out.println("enrolled with one walk-in: " + workshops.enrolled("records-and-sealed"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
