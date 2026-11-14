package practice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
            Logger jdkSerialization = Logger.getLogger("java.io.serialization");
            jdkSerialization.setLevel(Level.INFO);
            DisplaySettings settings = new DisplaySettings("alex.sample");
            System.out.println("before choosing: " + settings.timeZone());
            settings.setTimeZone("Pacific/Auckland");
            System.out.println("chosen: " + settings.timeZone());
            DisplaySettings onNodeB = replicate(settings);
            System.out.println("on the second node: " + onNodeB.user() + " " + onNodeB.timeZone());
            onNodeB.setTimeZone(null);
            System.out.println("after clearing, shown in: " + onNodeB.timeZone().orElse("server zone"));
            jdkSerialization.setLevel(null);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    /** What session replication does: write the object on one node, read it on another. */
    static DisplaySettings replicate(DisplaySettings settings) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(settings);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (DisplaySettings) in.readObject();
        }
    }
}
