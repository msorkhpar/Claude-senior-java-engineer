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
            Discussion.Comment post = new Discussion.Comment("post");
            Discussion.Comment q = post.reply("question");
            q.reply("answer").reply("thanks");
            q.reply("follow-up");
            post.reply("joke").reply("groan");
            System.out.println("display order: " + Discussion.inOrder(post));
            System.out.println("deepest: " + Discussion.deepest(post));
            System.out.println("summary: " + Discussion.summary(post));
            Discussion.Comment bot = new Discussion.Comment("spam-0");
            Discussion.Comment last = bot;
            for (int i = 1; i < 180_000; i++) last = last.reply("spam-" + i);
            System.out.println("bot thread: " + Discussion.summary(bot));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
