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
            List<Standings.Team> fixtures = new ArrayList<>(List.of(new Standings.Team("Dockside", 12),
                    new Standings.Team("Elm Park", 20), new Standings.Team("Fenwick", 12), new Standings.Team("Granby", 25)));
            List<Standings.Team> table = Standings.table(fixtures);
            System.out.println("table: " + names(table));
            System.out.println("fixtures afterwards: " + names(fixtures));
            fixtures.add(new Standings.Team("Hollow FC", 30));
            System.out.println("table after a team is added to the list: " + table.size() + " teams");
            System.out.println("from a fixed list: " + names(Standings.table(List.of(new Standings.Team("Ivybridge", 3),
                    new Standings.Team("Junction", 9)))));
            System.out.println("leaders: " + names(Standings.leaders(fixtures, 2)));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** Each team as "name points", in the list's order. */
    private static List<String> names(List<Standings.Team> teams) {
        return teams.stream().map(t -> t.name() + " " + t.points()).toList();
    }
}
