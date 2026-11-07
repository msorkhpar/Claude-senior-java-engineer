package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which guest holds which seat. The box office shares one store between its request threads;
 * the overnight re-seating tool and the unit tests each run on one thread. The callers only
 * ever see this class, so the map behind it is chosen by the factory method alone.
 */
public final class SeatHolds {
    private final Map<String, String> holds;

    private SeatHolds(Map<String, String> holds) {
        this.holds = holds;
    }

    /** For the box office: shared by many threads. */
    public static SeatHolds shared() {
        return new SeatHolds(new ConcurrentHashMap<>());
    }

    /** For the re-seating tool: one thread, seats kept sorted. */
    public static SeatHolds sorted() {
        return new SeatHolds(new TreeMap<>());
    }

    /** For tests and other single-threaded use. */
    public static SeatHolds plain() {
        return new SeatHolds(new HashMap<>());
    }

    /** Frees the seat; true if someone held it. */
    public boolean release(String seat) {
        return holds.remove(Objects.requireNonNull(seat, "seat")) != null;
    }

    /** Records that the guest holds the seat, replacing any earlier holder. */
    public void hold(String seat, String guest) {
        holds.put(Objects.requireNonNull(seat, "seat"), Objects.requireNonNull(guest, "guest"));
    }

    /** The guest holding the seat, or empty if it is free. */
    public Optional<String> holder(String seat) {
        return Optional.ofNullable(holds.get(Objects.requireNonNull(seat, "seat")));
    }

    /** Every hold as it is now; what happens to the store afterwards doesn't show in it. */
    public Map<String, String> holds() {
        return Map.copyOf(holds);
    }
}
