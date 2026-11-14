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
            String dump = "Full thread dump (trimmed):\n\n"
                    + "\"ledger-sync\" #12 waiting on condition\n"
                    + "   java.lang.Thread.State: TIMED_WAITING (sleeping)\n"
                    + "\tat java.lang.Thread.sleep(Native Method)\n"
                    + "\tat Shop.sync(Shop.java:10)\n"
                    + "\t- locked <0x00000007aa000010> (a Shop$Ledger)\n"
                    + "\t- locked <0x00000007aa000020> (a Shop$Till)\n"
                    + "\n"
                    + "\"cashier-1\" #13 waiting for monitor entry\n"
                    + "   java.lang.Thread.State: BLOCKED (on object monitor)\n"
                    + "\tat Shop.pay(Shop.java:20)\n"
                    + "\t- waiting to lock <0x00000007aa000020> (a Shop$Till)\n"
                    + "\n"
                    + "\"reporter\" #14 in Object.wait()\n"
                    + "   java.lang.Thread.State: WAITING (on object monitor)\n"
                    + "\tat java.lang.Object.wait0(Native Method)\n"
                    + "\t- waiting on <0x00000007aa000030> (a Shop$Queue)\n"
                    + "\tat Shop.report(Shop.java:30)\n"
                    + "\t- locked <0x00000007aa000030> (a Shop$Queue)\n"
                    + "\n"
                    + "\"cashier-2\" #15 waiting for monitor entry\n"
                    + "   java.lang.Thread.State: BLOCKED (on object monitor)\n"
                    + "\tat Shop.enqueue(Shop.java:40)\n"
                    + "\t- waiting to lock <0x00000007aa000030> (a Shop$Queue)\n"
                    + "\n";
            for (DumpReader.ThreadEntry t : DumpReader.threads(dump)) {
                System.out.println(t.name() + " waits for " + DumpReader.waitingFor(t) + ", holds " + DumpReader.held(t));
            }
            System.out.println("who blocks whom: " + DumpReader.whoBlocksWhom(dump));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
