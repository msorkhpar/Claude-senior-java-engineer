package practice;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum OrderState {
    CREATED {
        @Override
        public Set<OrderState> validTransitions() {
            return EnumSet.of(PENDING_PAYMENT, CANCELLED);
        }
    },
    PENDING_PAYMENT {
        @Override
        public Set<OrderState> validTransitions() {
            return EnumSet.of(PAID, CANCELLED);
        }
    },
    PAID {
        @Override
        public Set<OrderState> validTransitions() {
            return EnumSet.of(PROCESSING, REFUNDED);
        }
    },
    PROCESSING {
        @Override
        public Set<OrderState> validTransitions() {
            return EnumSet.of(SHIPPED, CANCELLED);
        }
    },
    SHIPPED {
        @Override
        public Set<OrderState> validTransitions() {
            return EnumSet.of(DELIVERED, RETURNED);
        }
    },
    DELIVERED {
        @Override
        public Set<OrderState> validTransitions() {
            return EnumSet.of(RETURNED);
        }
    },
    CANCELLED {
        @Override
        public Set<OrderState> validTransitions() {
            return EnumSet.noneOf(OrderState.class);
        }
    },
    REFUNDED {
        @Override
        public Set<OrderState> validTransitions() {
            return EnumSet.noneOf(OrderState.class);
        }
    },
    RETURNED {
        @Override
        public Set<OrderState> validTransitions() {
            return EnumSet.of(REFUNDED);
        }
    };

    /** The states this state may move to. */
    public abstract Set<OrderState> validTransitions();

    /** Whether {@code target} is one of this state's valid transitions. */
    public boolean canTransitionTo(OrderState target) {
        return validTransitions().contains(target);
    }

    /** Moves to {@code target}, refusing a transition this state does not allow. */
    public OrderState transitionTo(OrderState target) {
        return target;
    }

    /** Whether this state has no valid transitions. */
    public boolean isTerminal() {
        return validTransitions().isEmpty();
    }
}
