package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
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
            Map<String, Integer> stock = new HashMap<>(Map.of("CHAIR-1", 1));
            Consumer<OrderPipeline.Order> reserve = order -> {
                int left = stock.get(order.sku());
                if (left < order.quantity()) throw new IllegalStateException("no " + order.sku() + " left for " + order.id());
                stock.put(order.sku(), left - order.quantity());
                System.out.println("reserved " + order.id());
            };
            Consumer<OrderPipeline.Order> email = order -> System.out.println("emailed about " + order.id());
            Consumer<OrderPipeline.Order> audit = order -> System.out.println("compliance log: " + order.id());
            Consumer<OrderPipeline.Order> production = OrderPipeline.build(reserve, email, audit);
            production.accept(new OrderPipeline.Order("B-1", "CHAIR-1", 1));
            try {
                production.accept(new OrderPipeline.Order("B-2", "CHAIR-1", 1));
            } catch (IllegalStateException refused) {
                System.out.println("checkout shows: " + refused.getMessage());
            }
            stock.put("CHAIR-1", 5);
            OrderPipeline.build(reserve, email, null).accept(new OrderPipeline.Order("S-1", "CHAIR-1", 2));
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }
}
