package practice;

import java.util.Set;
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
            PermissionGuard guard = new PermissionGuard();
            System.out.println("refund needs: " + guard.required(Payouts.class.getDeclaredMethod("refund")));
            System.out.println("balance needs: " + guard.required(Payouts.class.getDeclaredMethod("balance")));
            System.out.println("ledger needs: " + guard.required(Ledger.class.getDeclaredMethod("close")));
            System.out.println("ping needs: " + guard.required(Payouts.class.getDeclaredMethod("ping")));
            System.out.println("no permissions may refund: " + guard.mayCall(Payouts.class.getDeclaredMethod("refund"), Set.of()));
            System.out.println("finance:admin may refund: " + guard.mayCall(Payouts.class.getDeclaredMethod("refund"), Set.of("finance:admin")));
            System.out.println("payouts:read may close ledger: " + guard.mayCall(Ledger.class.getDeclaredMethod("close"), Set.of("payouts:read")));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static class Payouts {
        @PermissionGuard.Permission("payouts:read")
        public void balance() { }
        @PermissionGuard.Permission("payouts:refund")
        @PermissionGuard.Permission("finance:admin")
        public void refund() { }
        public void ping() { }
    }
    @PermissionGuard.Permission("ledger:close")
    @PermissionGuard.Permission("finance:admin")
    static class Ledger {
        public void close() { }
    }
}
