package practice;

/** Support tickets, loaded from the old table into one record per state. */
public class Tickets {

    public sealed interface Ticket permits Open, Assigned, Resolved {
        String id();
    }

    public record Open(String id) implements Ticket {
        public Open {
            requireText(id, "id");
        }
    }

    public record Assigned(String id, String agent) implements Ticket {
        public Assigned {
            // TODO: refuse an Assigned that lacks what an assigned ticket must have.
            throw new IllegalStateException("TODO: Assigned");
        }
    }

    public record Resolved(String id, String agent, String resolution) implements Ticket {
        public Resolved {
            // TODO: refuse a Resolved that lacks what a resolved ticket must have.
            throw new IllegalStateException("TODO: Resolved");
        }
    }

    /** One row of the old table: a status and two columns that may be null. */
    public record Row(String id, String status, String agent, String resolution) {}

    static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is missing");
        }
    }

    /**
     * Loads one row. A row whose columns don't fit its status is refused with an
     * IllegalArgumentException whose message names the row's id.
     */
    public static Ticket fromRow(Row row) {
        // TODO: turn the row into the ticket its status names, or refuse it.
        throw new IllegalStateException("TODO: fromRow");
    }

    static void requireEmpty(String value, String name) {
        if (value != null) {
            throw new IllegalArgumentException(name + " set on a ticket in this state");
        }
    }

    /** Writes a ticket back as a row. */
    public static Row toRow(Ticket t) {
        return switch (t) {
            case Open o -> new Row(o.id(), "OPEN", null, null);
            case Assigned a -> new Row(a.id(), "ASSIGNED", a.agent(), null);
            case Resolved r -> new Row(r.id(), "RESOLVED", r.agent(), r.resolution());
        };
    }
}
