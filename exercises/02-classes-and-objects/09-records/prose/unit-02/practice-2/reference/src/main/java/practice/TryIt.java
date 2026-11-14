package practice;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
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
            Map<String, Object> pickupRow = new LinkedHashMap<>();
            pickupRow.put("date", LocalDate.of(2029, 3, 8));
            pickupRow.put("customer", "  acme-12 ");
            pickupRow.put("boxes", 6);
            System.out.println("pickup: " + RowBinder.bind(Pickup.class, pickupRow));
            Map<String, Object> roomRow = new LinkedHashMap<>();
            roomRow.put("seats", 40);
            roomRow.put("name", "Hall B");
            System.out.println("room: " + RowBinder.bind(Room.class, roomRow));
            Map<String, Object> badRow = new LinkedHashMap<>();
            badRow.put("name", "Closet");
            badRow.put("seats", 0);
            try {
                RowBinder.bind(Room.class, badRow);
                System.out.println("room with 0 seats: bound");
            } catch (IllegalArgumentException refused) {
                System.out.println("room with 0 seats: refused, " + refused.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    public record Pickup(String customer, int boxes, LocalDate date) {
        public Pickup {
            customer = customer.strip().toUpperCase();
        }
    }

    public record Room(String name, int seats) {
        public Room {
            if (seats < 1) {
                throw new IllegalArgumentException("a room needs at least one seat: " + seats);
            }
        }
    }
}
