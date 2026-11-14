package practice;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            Map<DayOfWeek, List<String>> editor = new HashMap<>();
            List<String> tuesday = new ArrayList<>(List.of("Primary Pat", "Backup Bea"));
            editor.put(DayOfWeek.TUESDAY, tuesday);
            editor.put(DayOfWeek.THURSDAY, new ArrayList<>(List.of("Backup Bea", "Primary Pat")));
            List<String> escalation = new ArrayList<>(List.of("Lead Lou"));
            OnCall.Rota rota = new OnCall.Rota("search", editor, escalation);
            System.out.println("Tuesday page order when published: " + rota.pageOrder(DayOfWeek.TUESDAY));
            tuesday.set(0, "Backup Bea");
            tuesday.set(1, "Primary Pat");
            escalation.add("Director Dee");
            editor.remove(DayOfWeek.THURSDAY);
            System.out.println("Tuesday after the editor reordered: " + rota.pageOrder(DayOfWeek.TUESDAY));
            System.out.println("Thursday after the editor removed it: " + rota.pageOrder(DayOfWeek.THURSDAY));
            try {
                rota.escalation().add("Intern Ivo");
            } catch (UnsupportedOperationException refused) {
                System.out.println("handed-out escalation list refuses changes");
            }
            System.out.println("escalation now: " + rota.escalation());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
