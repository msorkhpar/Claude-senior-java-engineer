package practice;

import org.junit.jupiter.api.Test;
import practice.TicketStore.PreviousState;
import practice.TicketStore.State;

import static org.assertj.core.api.Assertions.assertThat;

class TicketStoreTest {

    @Test
    void aStateThisReleaseSavedLoadsBack() {
        TicketStore store = new TicketStore();
        store.save(501, State.OPEN);
        store.save(502, State.TRIAGED);
        store.save(503, State.CLOSED);

        assertThat(store.load(501)).isEqualTo(State.OPEN);
        assertThat(store.load(502)).isEqualTo(State.TRIAGED);
        assertThat(store.load(503)).isEqualTo(State.CLOSED);
    }

    @Test
    void savingAgainReplacesTheState() {
        TicketStore store = new TicketStore();
        store.save(77, State.ASSIGNED);
        store.save(77, State.RESOLVED);

        assertThat(store.load(77)).isEqualTo(State.RESOLVED);
    }

    @Test
    void rowsThePreviousReleaseWroteKeepTheirMeaning() {
        TicketStore store = new TicketStore();
        store.save(9001, PreviousState.ASSIGNED);
        store.save(9002, PreviousState.RESOLVED);
        store.save(9003, PreviousState.CLOSED);

        assertThat(store.load(9001)).isEqualTo(State.ASSIGNED);
        assertThat(store.load(9002)).isEqualTo(State.RESOLVED);
        assertThat(store.load(9003)).isEqualTo(State.CLOSED);
    }

    @Test
    void aStateWithItsOwnDisplayTextLoadsBack() {
        TicketStore store = new TicketStore();
        store.save(640, State.WAITING);

        assertThat(store.load(640)).isEqualTo(State.WAITING);
    }
}
