package practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps each support ticket's state in a text column of the tickets table. Rows written by the
 * previous release stay in the table, so this release must read them too. Some rows hold text
 * this release can't read: see stateLabel and countIn.
 */
public class TicketStore {

    /** The states as the previous release declared them. Its rows are still in the table. */
    public enum PreviousState { OPEN, ASSIGNED, RESOLVED, CLOSED }

    /** The states of this release: TRIAGED and WAITING are new. */
    public enum State {
        OPEN,
        TRIAGED,
        ASSIGNED,
        WAITING {
            @Override
            public String toString() { return "waiting for customer"; }
        },
        RESOLVED,
        CLOSED
    }

    /** The table: ticket id to the text in its state column. */
    private final Map<Long, String> stateColumn = new HashMap<>();

    /** Saves a ticket's state; the previous release's states arrive through here too. */
    public void save(long ticketId, Enum<?> state) {
        stateColumn.put(ticketId, write(state));
    }

    /** Copies a row in as it is, from an import or from a release newer than this one. */
    public void importRow(long ticketId, String column) {
        stateColumn.put(ticketId, column);
    }

    /** Loads a ticket's state as this release knows it. */
    public State load(long ticketId) {
        return read(stateColumn.get(ticketId));
    }

    /** The text that goes into the state column for a state of either release. */
    static String write(Enum<?> state) {
        return state.name();
    }

    /** The state of this release that a column's text stands for. */
    static State read(String column) {
        return State.valueOf(column);
    }

    /** What the agents' ticket list shows for a ticket's state; "unknown" for a row this release can't read. */
    public String stateLabel(long ticketId) {
        return tryRead(stateColumn.get(ticketId)).map(State::toString).orElse("unknown");
    }

    /** How many of the tickets are in the state; rows this release can't read are not counted. */
    public long countIn(List<Long> ticketIds, State state) {
        return ticketIds.stream()
                .map(id -> tryRead(stateColumn.get(id)))
                .filter(s -> s.equals(Optional.of(state)))
                .count();
    }

    /** The state a column's text stands for, or empty when this release has no such state. */
    static Optional<State> tryRead(String column) {
        try {
            return Optional.of(State.valueOf(column));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
