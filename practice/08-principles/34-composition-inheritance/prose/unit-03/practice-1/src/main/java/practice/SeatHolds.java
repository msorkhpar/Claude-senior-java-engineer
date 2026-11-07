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

    /**
     * Records that the guest holds the seat, replacing any earlier holder.
     * TODO: callers must see the same behaviour whichever map is behind the store.
     */
    public void hold(String seat, String guest) {
        throw new IllegalStateException("TODO: hold");
    }

    /**
     * The guest holding the seat, or empty if it is free.
     * TODO: callers must see the same behaviour whichever map is behind the store.
     */
    public Optional<String> holder(String seat) {
        throw new IllegalStateException("TODO: holder");
    }

    /**
     * Every hold as it is now; what happens to the store afterwards doesn't show in it.
     * TODO
     */
    public Map<String, String> holds() {
        throw new IllegalStateException("TODO: holds");
    }
}
