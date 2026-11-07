package practice;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** A help-desk ticket. Agents move it through the support team's workflow. */
public final class Ticket {

    public enum State {
        NEW, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED;

        private static final Map<State, Set<State>> NEXT = transitions();

        /** The support team's workflow: which states each state may move to. */
        private static Map<State, Set<State>> transitions() {
            // TODO: Build the workflow the statement describes, so that every state can answer
            //       canMoveTo and allowedNext.
            throw new IllegalStateException("TODO: transitions");
        }

        /** Whether the workflow lets a ticket in this state move to the target. */
        public boolean canMoveTo(State target) {
            // TODO: Answer from the workflow, for a ticket in any state.
            throw new IllegalStateException("TODO: canMoveTo");
        }

        /** The states this one may move to; the agent's screen shows one button for each. */
        public Set<State> allowedNext() {
            // TODO: Hand out the states this one may move to. Whatever a screen does with the set
            //       must not change the workflow.
            throw new IllegalStateException("TODO: allowedNext");
        }
    }

    private State state = State.NEW;

    public State state() {
        return state;
    }

    /** Applies a change an agent asked for. */
    public void moveTo(State target) {
        // TODO: Apply the change if the workflow allows it. A change it doesn't allow must fail
        //       with an IllegalStateException and leave the ticket as it was.
        throw new IllegalStateException("TODO: moveTo");
    }
}
