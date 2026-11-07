package practice;

import org.junit.jupiter.api.Test;
import practice.TicketStore.PreviousState;
import practice.TicketStore.State;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TicketStoreTest {

    @Test
    void rowsThisReleaseCanReadAreLabelledAndCounted() {
        TicketStore store = new TicketStore();
        store.save(310, State.TRIAGED);
        store.save(311, PreviousState.CLOSED);
        store.save(312, State.CLOSED);
        store.importRow(313, "ASSIGNED");

        assertThat(store.stateLabel(310)).isEqualTo("TRIAGED");
        assertThat(store.stateLabel(313)).isEqualTo("ASSIGNED");
        assertThat(store.countIn(List.of(310L, 311L, 312L, 313L), State.CLOSED)).isEqualTo(2);
        assertThat(TicketStore.tryRead("RESOLVED")).isEqualTo(Optional.of(State.RESOLVED));
    }

    @Test
    void aStateANewerReleaseWroteIsUnknownHere() {
        TicketStore store = new TicketStore();
        store.save(820, State.OPEN);
        store.importRow(821, "ESCALATED");
        store.save(822, State.OPEN);

        assertThat(store.stateLabel(821)).isEqualTo("unknown");
        assertThat(store.countIn(List.of(820L, 821L, 822L), State.OPEN)).isEqualTo(2);
        assertThat(TicketStore.tryRead("ON_SITE")).isEmpty();
    }

    @Test
    void anEmptyColumnIsUnknownHere() {
        TicketStore store = new TicketStore();
        store.importRow(1205, null);
        store.save(1206, State.ASSIGNED);

        assertThat(store.stateLabel(1205)).isEqualTo("unknown");
        assertThat(store.countIn(List.of(1205L, 1206L), State.ASSIGNED)).isEqualTo(1);
    }

    @Test
    void aStateWithItsOwnDisplayTextIsReadByItsStoredText() {
        TicketStore store = new TicketStore();
        store.save(47, State.WAITING);

        assertThat(store.stateLabel(47)).isEqualTo("waiting for customer");
        assertThat(store.countIn(List.of(47L), State.WAITING)).isEqualTo(1);
    }
}
