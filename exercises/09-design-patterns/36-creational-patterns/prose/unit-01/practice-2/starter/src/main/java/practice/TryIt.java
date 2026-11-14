package practice;

import java.net.URL;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
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
            URL[] jar = {TryIt.class.getProtectionDomain().getCodeSource().getLocation()};
            int before = PluginHost.Quota.INSTANCE.left();
            PluginHost.Plugin billing = PluginHost.load(jar, "practice.TryIt$BillingPlugin");
            PluginHost.Plugin invoices = PluginHost.load(jar, "practice.TryIt$BillingPlugin");
            System.out.println("billing: " + billing.run());
            System.out.println("billing: " + billing.run());
            System.out.println("invoices: " + invoices.run());
            System.out.println("host quota used: " + (before - PluginHost.Quota.INSTANCE.left()));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** A plugin that counts its own runs and spends three partner calls per run. */
    public static class BillingPlugin implements PluginHost.Plugin {
        private static int runs;
        @Override
        public String run() {
            int made = 0;
            for (int i = 0; i < 3; i++) if (PluginHost.Quota.INSTANCE.tryTake()) made++;
            return "run " + (++runs) + ", calls made " + made;
        }
    }
}
