package practice;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum OrderState {
    CREATED, PENDING_PAYMENT, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED, RETURNED;

    private static final Map<OrderState, Set<OrderState>> TABLE = new EnumMap<>(OrderState.class);

    static {
        TABLE.put(CREATED, EnumSet.of(PENDING_PAYMENT, CANCELLED));
        TABLE.put(PENDING_PAYMENT, EnumSet.of(PAID, CANCELLED));
        TABLE.put(PAID, EnumSet.of(PROCESSING, REFUNDED));
        TABLE.put(PROCESSING, EnumSet.of(SHIPPED, CANCELLED));
        TABLE.put(SHIPPED, EnumSet.of(DELIVERED, RETURNED));
        TABLE.put(DELIVERED, EnumSet.of(RETURNED));
        TABLE.put(CANCELLED, EnumSet.noneOf(OrderState.class));
        TABLE.put(REFUNDED, EnumSet.noneOf(OrderState.class));
        TABLE.put(RETURNED, EnumSet.of(REFUNDED));
    }

    /** The states this state may move to. */
    public Set<OrderState> validTransitions() {
        return TABLE.get(this);
    }

    /** Whether {@code target} is one of this state's valid transitions. */
    public boolean canTransitionTo(OrderState target) {
        return validTransitions().contains(target);
    }

    /** Moves to {@code target}, refusing a transition this state does not allow. */
    public OrderState transitionTo(OrderState target) {
        if (!canTransitionTo(target)) {
            throw new IllegalStateException("Cannot transition from " + this + " to " + target);
        }
        return target;
    }

    /** Whether this state has no valid transitions. */
    public boolean isTerminal() {
        return validTransitions().isEmpty();
    }
}
