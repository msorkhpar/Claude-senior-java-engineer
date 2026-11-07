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
            Map<State, Set<State>> next = new EnumMap<>(State.class);
            next.put(NEW, EnumSet.of(ASSIGNED, CLOSED));
            next.put(ASSIGNED, EnumSet.of(IN_PROGRESS, NEW));
            next.put(IN_PROGRESS, EnumSet.of(RESOLVED, ASSIGNED));
            next.put(RESOLVED, EnumSet.of(CLOSED, IN_PROGRESS));
            next.put(CLOSED, EnumSet.noneOf(State.class));
            return next;
        }

        /** Whether the workflow lets a ticket in this state move to the target. */
        public boolean canMoveTo(State target) {
            return NEXT.get(this).contains(target);
        }

        /** The states this one may move to; the agent's screen shows one button for each. */
        public Set<State> allowedNext() {
            return NEXT.get(this);
        }
    }

    private State state = State.NEW;

    public State state() {
        return state;
    }

    /** Applies a change an agent asked for. */
    public void moveTo(State target) {
        if (!state.canMoveTo(target)) {
            throw new IllegalStateException("ticket can't move from " + state + " to " + target);
        }
        state = target;
    }
}
