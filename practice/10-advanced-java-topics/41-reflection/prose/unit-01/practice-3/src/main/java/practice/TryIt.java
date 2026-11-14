package practice;

import java.util.ArrayList;
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
            Gateway gateway = new Gateway();
            SettingsBinder.bind(gateway, Map.of("adminPorts", "7001,7002"));
            SettingsBinder.bind(gateway, Map.of("timeoutsMillis", "500,2500"));
            SettingsBinder.bind(gateway, Map.of("hosts", "edge-a.example.invalid, edge-b.example.invalid"));
            SettingsBinder.bind(gateway, Map.of("backupPorts", "6001"));
            int first = gateway.adminPorts.get(0);
            System.out.println("admin ports: " + gateway.adminPorts + ", first as int: " + first);
            System.out.println("timeouts: " + gateway.timeoutsMillis + " (" + gateway.timeoutsMillis.get(0).getClass().getSimpleName() + ")");
            System.out.println("hosts: " + gateway.hosts);
            System.out.println("backup ports: " + gateway.backupPorts + " (" + gateway.backupPorts.get(0).getClass().getSimpleName() + ")");
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static class SlotList extends ArrayList<Integer> {}
    static class Gateway {
        List<Integer> adminPorts = new ArrayList<>();
        List<Long> timeoutsMillis = new ArrayList<>(List.of(1L));
        List<String> hosts = new ArrayList<>();
        SlotList backupPorts = new SlotList();
    }
}
