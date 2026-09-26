package practice;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Runs in name order, so the test that tries to change a transition set runs last. */
@TestMethodOrder(MethodOrderer.MethodName.class)
class OrderStateTest {

    @Test
    void followsTheOrderLifecycle() {
        OrderState state = OrderState.CREATED;
        state = state.transitionTo(OrderState.PENDING_PAYMENT);
        state = state.transitionTo(OrderState.PAID);
        state = state.transitionTo(OrderState.PROCESSING);
        state = state.transitionTo(OrderState.SHIPPED);
        state = state.transitionTo(OrderState.DELIVERED);
        assertThat(state).isEqualTo(OrderState.DELIVERED);
        assertThat(OrderState.RETURNED.validTransitions()).containsExactly(OrderState.REFUNDED);
        assertThat(OrderState.PAID.canTransitionTo(OrderState.REFUNDED)).isTrue();
        assertThat(OrderState.SHIPPED.canTransitionTo(OrderState.CANCELLED)).isFalse();
        assertThat(OrderState.CANCELLED.isTerminal()).isTrue();
        assertThat(OrderState.REFUNDED.isTerminal()).isTrue();
        assertThat(OrderState.DELIVERED.isTerminal()).isFalse();
        assertThat(OrderState.CREATED.validTransitions()).containsExactlyInAnyOrder(OrderState.PENDING_PAYMENT, OrderState.CANCELLED);
        assertThat(OrderState.PENDING_PAYMENT.validTransitions()).containsExactlyInAnyOrder(OrderState.PAID, OrderState.CANCELLED);
        assertThat(OrderState.PAID.validTransitions()).containsExactlyInAnyOrder(OrderState.PROCESSING, OrderState.REFUNDED);
        assertThat(OrderState.PROCESSING.validTransitions()).containsExactlyInAnyOrder(OrderState.SHIPPED, OrderState.CANCELLED);
        assertThat(OrderState.SHIPPED.validTransitions()).containsExactlyInAnyOrder(OrderState.DELIVERED, OrderState.RETURNED);
        assertThat(OrderState.DELIVERED.validTransitions()).containsExactly(OrderState.RETURNED);
        assertThat(OrderState.CANCELLED.validTransitions()).isEmpty();
        assertThat(OrderState.REFUNDED.validTransitions()).isEmpty();
    }

    @Test
    void anInvalidTransitionIsRefused() {
        assertThatThrownBy(() -> OrderState.CREATED.transitionTo(OrderState.DELIVERED))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> OrderState.CANCELLED.transitionTo(OrderState.CREATED))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void validTransitionsCannotBeChangedByACaller() {
        Set<OrderState> seen = OrderState.CREATED.validTransitions();
        try {
            seen.add(OrderState.DELIVERED);
        } catch (UnsupportedOperationException refused) {
            // a read-only set is one way to keep the table safe
        }
        assertThat(OrderState.CREATED.canTransitionTo(OrderState.DELIVERED)).isFalse();
        assertThatThrownBy(() -> OrderState.CREATED.transitionTo(OrderState.DELIVERED))
                .isInstanceOf(IllegalStateException.class);
        Set<OrderState> none = OrderState.CANCELLED.validTransitions();
        try {
            none.add(OrderState.CREATED);
        } catch (UnsupportedOperationException refused) {
            // a read-only set is one way to keep the table safe
        }
        assertThat(OrderState.CANCELLED.isTerminal()).isTrue();
        assertThat(OrderState.REFUNDED.isTerminal()).isTrue();
        assertThat(OrderState.CANCELLED.canTransitionTo(OrderState.CREATED)).isFalse();
    }
}
