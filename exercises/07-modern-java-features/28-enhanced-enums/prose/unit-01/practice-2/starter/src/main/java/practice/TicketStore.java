package practice;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps each support ticket's state in a text column of the tickets table. Rows written by the
 * previous release stay in the table, so this release must read them too.
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

    /** Loads a ticket's state as this release knows it. */
    public State load(long ticketId) {
        return read(stateColumn.get(ticketId));
    }

    /** The text that goes into the state column for a state of either release. */
    static String write(Enum<?> state) {
        // TODO: Choose the text that stands for this state in the column. A row is read back
        //       by later releases of the service, not only by the one that wrote it.
        throw new IllegalStateException("TODO: write");
    }

    /** The state of this release that a column's text stands for. */
    static State read(String column) {
        // TODO: Turn the column's text back into the state it stands for, in this release.
        throw new IllegalStateException("TODO: read");
    }
}
