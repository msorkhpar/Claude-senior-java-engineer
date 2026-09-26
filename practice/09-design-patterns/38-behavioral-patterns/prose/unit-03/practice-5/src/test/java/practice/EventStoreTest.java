package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import practice.EventStore.Account;
import practice.EventStore.AccountCommand;
import practice.EventStore.Deposit;
import practice.EventStore.Withdraw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventStoreTest {

    private static EventStore storeWithThree(Account live) {
        EventStore store = new EventStore(live);
        store.append(new Deposit(500, 100));
        store.append(new Withdraw(200, 200));
        store.append(new Deposit(1000, 300));
        return store;
    }

    @Test
    void appendsAndReplays() {
        Account live = new Account();
        EventStore store = storeWithThree(live);

        assertThat(live.balance()).isEqualTo(1300);
        assertThat(store.replay().balance()).isEqualTo(1300);
        assertThat(store.events()).containsExactly(new Deposit(500, 100), new Withdraw(200, 200), new Deposit(1000, 300));
        assertThat(store.eventsSince(0)).hasSize(3);

        EventStore late = new EventStore(new Account());
        late.append(new Deposit(100, 300));
        late.append(new Withdraw(100, 200));
        assertThat(late.replay().balance()).isZero();
    }

    @Test
    void replayStartsFromAFreshAccount() {
        Account live = new Account();
        EventStore store = storeWithThree(live);

        Account first = store.replay();
        Account second = store.replay();

        assertThat(first).isNotSameAs(live);
        assertThat(first.balance()).isEqualTo(1300);
        assertThat(second.balance()).isEqualTo(1300);
        assertThat(live.balance()).isEqualTo(1300);
    }

    @Test
    void aRejectedCommandIsNotLogged() {
        Account live = new Account();
        EventStore store = storeWithThree(live);

        assertThatThrownBy(() -> store.append(new Withdraw(5000, 400))).isInstanceOf(IllegalStateException.class);

        assertThat(live.balance()).isEqualTo(1300);
        assertThat(store.events()).hasSize(3);
        assertThat(store.replay().balance()).isEqualTo(1300);
    }

    @Test
    void eventsSinceIsStrictlyAfter() {
        EventStore store = storeWithThree(new Account());

        assertThat(store.eventsSince(200)).containsExactly(new Deposit(1000, 300));
        assertThat(store.eventsSince(300)).isEmpty();
    }

    @Test
    void theLogACallerHoldsDoesNotGrow() {
        EventStore store = new EventStore(new Account());
        store.append(new Deposit(500, 100));

        List<AccountCommand> seen = store.events();
        store.append(new Deposit(700, 200));

        assertThat(seen).containsExactly(new Deposit(500, 100));
        assertThat(store.events()).hasSize(2);
    }
}
