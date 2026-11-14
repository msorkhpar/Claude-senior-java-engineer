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
            received.clear();
            EventBus bus = new EventBus();
            bus.register(new Newsletter());
            bus.register(new Chargebacks());
            bus.publish(new Signup("noor"));
            bus.publish(new Abandoned("cart-52"));
            bus.publish(new Chargeback("cb-9"));
            System.out.println("subscribers of Newsletter: " + EventBus.subscriberMethods(Newsletter.class).size());
            for (String line : received) System.out.println("delivered: " + line);
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static final java.util.List<String> received = new java.util.ArrayList<>();
    record Signup(String user) {}
    record Abandoned(String cartId) {}
    record Chargeback(String id) {}
    abstract static class Tracked {
        @EventBus.Subscribe
        public void track(Signup e) { received.add("track " + e.user()); }
    }
    static class Newsletter extends Tracked {
        @EventBus.Subscribe
        void greet(Signup e) { received.add("greet " + e.user()); }
        @EventBus.Subscribe
        private void nudge(Abandoned e) { received.add("nudge " + e.cartId()); }
        public void ignored(Signup e) { received.add("ignored " + e.user()); }
    }
    interface Handler<E> { void on(E event); }
    static class Chargebacks implements Handler<Chargeback> {
        @EventBus.Subscribe
        @Override
        public void on(Chargeback e) { received.add("dispute " + e.id()); }
    }
}
