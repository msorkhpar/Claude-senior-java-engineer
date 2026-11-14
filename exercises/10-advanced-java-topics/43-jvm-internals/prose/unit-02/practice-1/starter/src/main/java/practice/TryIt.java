package practice;

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
            List<String> stays = List.of("iload_0", "iinc          0, 1", "istore_0", "iload_0", "ireturn");
            List<String> mixed = List.of("iload_0", "iinc          0, 5", "iload_0", "imul", "ireturn");
            List<String> average = List.of("iload_0", "iload_1", "iadd", "iload_2", "isub", "iconst_2", "idiv", "ireturn");
            List<String> stepDown = List.of("iinc          0, -4", "iload_0", "bipush        3", "imul", "ireturn");
            System.out.println("x = x++ with x=6 returns: " + IntMachine.run(stays, 6));
            System.out.println("x++ * (x+=5) with x=2 returns: " + IntMachine.run(mixed, 2));
            System.out.println("(a + b - c) / 2 with 50, 14, 9 returns: " + IntMachine.run(average, 50, 14, 9));
            System.out.println("(x -= 4) * 3 with x=10 returns: " + IntMachine.run(stepDown, 10));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
