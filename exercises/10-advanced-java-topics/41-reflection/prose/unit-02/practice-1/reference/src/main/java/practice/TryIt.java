package practice;

import java.util.ArrayList;
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
            System.out.println("order: " + Snapshot.render(new Order("ORD-7", 3, new Customer("Oslo", "0150"))));
            System.out.println("tote: " + Snapshot.render(new Tote()));
            System.out.println("stops: " + Snapshot.render(new Stops("DHL", List.of("OSL", "BGO"))));
            System.out.println("customer fields open: " + Snapshot.openFields(Customer.class).map(List::size).orElse(-1));
            System.out.println("ArrayList fields open: " + Snapshot.openFields(ArrayList.class).isPresent());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    static class Line {
        private final String ref;
        Line(String ref) { this.ref = ref; }
    }
    static class Customer {
        private final String city;
        private final String zip;
        Customer(String city, String zip) { this.city = city; this.zip = zip; }
    }
    static class Order extends Line {
        private final int boxes;
        private final Customer customer;
        Order(String ref, int boxes, Customer customer) { super(ref); this.boxes = boxes; this.customer = customer; }
    }
    static class Tote {
        private final String id = "T-5";
        private final ArrayList<String> items = new ArrayList<>(List.of("mug", "lamp"));
    }
    static class Stops extends ArrayList<String> {
        private final String carrier;
        Stops(String carrier, List<String> stops) { super(stops); this.carrier = carrier; }
    }
}
