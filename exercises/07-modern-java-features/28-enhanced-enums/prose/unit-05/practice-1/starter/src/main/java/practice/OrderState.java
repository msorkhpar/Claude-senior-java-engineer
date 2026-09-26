package practice;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum OrderState {
    CREATED {
        @Override
        public Set<OrderState> validTransitions() {
            throw new UnsupportedOperationException("write validTransitions");
        }
    },
    PENDING_PAYMENT {
        @Override
        public Set<OrderState> validTransitions() {
            throw new UnsupportedOperationException("write validTransitions");
        }
    },
    PAID {
        @Override
        public Set<OrderState> validTransitions() {
            throw new UnsupportedOperationException("write validTransitions");
        }
    },
    PROCESSING {
        @Override
        public Set<OrderState> validTransitions() {
            throw new UnsupportedOperationException("write validTransitions");
        }
    },
    SHIPPED {
        @Override
        public Set<OrderState> validTransitions() {
            throw new UnsupportedOperationException("write validTransitions");
        }
    },
    DELIVERED {
        @Override
        public Set<OrderState> validTransitions() {
            throw new UnsupportedOperationException("write validTransitions");
        }
    },
    CANCELLED {
        @Override
        public Set<OrderState> validTransitions() {
            throw new UnsupportedOperationException("write validTransitions");
        }
    },
    REFUNDED {
        @Override
        public Set<OrderState> validTransitions() {
            throw new UnsupportedOperationException("write validTransitions");
        }
    },
    RETURNED {
        @Override
        public Set<OrderState> validTransitions() {
            throw new UnsupportedOperationException("write validTransitions");
        }
    };

    /** The states this state may move to. */
    public abstract Set<OrderState> validTransitions();

    /** Whether {@code target} is one of this state's valid transitions. */
    public boolean canTransitionTo(OrderState target) {
        throw new UnsupportedOperationException("write canTransitionTo");
    }

    /** Moves to {@code target}, refusing a transition this state does not allow. */
    public OrderState transitionTo(OrderState target) {
        throw new UnsupportedOperationException("write transitionTo");
    }

    /** Whether this state has no valid transitions. */
    public boolean isTerminal() {
        throw new UnsupportedOperationException("write isTerminal");
    }
}
