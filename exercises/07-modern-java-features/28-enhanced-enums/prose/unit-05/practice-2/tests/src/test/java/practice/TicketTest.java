package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;
import practice.Ticket.State;

class TicketTest {

    @Test
    void aTicketWalksThroughTheWorkflowAndCanBeReopened() {
        Ticket t = new Ticket();
        t.moveTo(State.ASSIGNED);
        t.moveTo(State.IN_PROGRESS);
        t.moveTo(State.RESOLVED);
        t.moveTo(State.IN_PROGRESS);             // the customer says it isn't fixed
        t.moveTo(State.RESOLVED);
        t.moveTo(State.CLOSED);
        assertThat(t.state()).isEqualTo(State.CLOSED);
    }

    @Test
    void theWorkflowAnswersWhatMayFollow() {
        assertThat(State.ASSIGNED.canMoveTo(State.NEW)).isTrue();
        assertThat(State.IN_PROGRESS.canMoveTo(State.CLOSED)).isFalse();
        assertThat(State.RESOLVED.allowedNext()).containsExactlyInAnyOrder(State.CLOSED, State.IN_PROGRESS);
    }

    @Test
    void aClosedTicketHasNowhereToGo() {
        for (State target : State.values()) {
            assertThat(State.CLOSED.canMoveTo(target)).as("CLOSED -> %s", target).isFalse();
        }
        assertThat(State.CLOSED.allowedNext()).isEmpty();
    }

    @Test
    void aChangeTheWorkflowRefusesLeavesTheTicketAsItWas() {
        Ticket t = new Ticket();
        t.moveTo(State.CLOSED);                  // closed as spam
        assertThatThrownBy(() -> t.moveTo(State.IN_PROGRESS)).isInstanceOf(IllegalStateException.class);
        assertThat(t.state()).isEqualTo(State.CLOSED);
    }

    @Test
    void aScreenThatEditsItsButtonsDoesNotChangeTheWorkflow() {
        Set<State> buttons = State.ASSIGNED.allowedNext();
        try {
            buttons.add(State.CLOSED);
        } catch (UnsupportedOperationException refused) {
            // refusing the change is one right answer
        }
        assertThat(State.ASSIGNED.canMoveTo(State.CLOSED)).isFalse();
    }
}
