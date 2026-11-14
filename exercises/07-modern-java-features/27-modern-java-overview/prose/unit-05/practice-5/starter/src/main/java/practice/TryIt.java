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
            String star = new String(Character.toChars(0x1F31F));
            String smile = "\u263A";
            System.out.println("'bond_007' has a picture: " + Nicknames.hasPicture("bond_007"));
            System.out.println("'#crew*5' has a picture: " + Nicknames.hasPicture("#crew*5"));
            System.out.println("'Ana' + U+263A has a picture: " + Nicknames.hasPicture("Ana" + smile));
            System.out.println("'shine' + U+1F31F has a picture: " + Nicknames.hasPicture("shine" + star));
            System.out.println("underline of 'shine' + U+1F31F (6 shown): " + Nicknames.underline("shine" + star));
            System.out.println("underline of 'Zoe' spelled with U+00EB (3 shown): " + Nicknames.underline("Zo\u00EB"));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
