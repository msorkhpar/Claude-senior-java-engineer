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
            TypeRegistry media = TypeRegistry.of(Media.class);
            System.out.println("media classes: " + media.registered().stream().map(Class::getSimpleName).sorted().toList());
            System.out.println("type id of a podcast: " + media.typeId(new Podcast("weekly")));
            System.out.println("type id of an audiobook: " + media.typeId(new AudioBook()));
            try {
                TypeRegistry.of(Discount.class);
                System.out.println("discounts: registered");
            } catch (IllegalArgumentException refused) {
                System.out.println("discounts: refused, " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    sealed interface Media permits Video, Audio {}

    record Video(int seconds) implements Media {}

    abstract static sealed class Audio implements Media permits Podcast, Track {}

    static final class Podcast extends Audio {
        final String show;

        Podcast(String show) { this.show = show; }
    }

    static sealed class Track extends Audio permits AudioBook {}

    static final class AudioBook extends Track {}

    sealed interface Discount permits Coupon, PartnerDiscount {}

    record Coupon(String code) implements Discount {}

    static non-sealed class PartnerDiscount implements Discount {}
}
