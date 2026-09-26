package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionsTest {

    interface OrderService {
        @Transactions.Transactional
        String placeOrder(String id);

        String describe(String id);

        @Transactions.Transactional
        int importOrders(String file) throws java.io.IOException;
    }

    /** Target and transaction manager write to one shared list of events. */
    static final class Events implements Transactions.TxManager {
        final List<String> seen = new ArrayList<>();

        @Override
        public void begin() {
            seen.add("begin");
        }

        @Override
        public void commit() {
            seen.add("commit");
        }

        @Override
        public void rollback() {
            seen.add("rollback");
        }
    }

    static final class Orders implements OrderService {
        private final Events events;

        Orders(Events events) {
            this.events = events;
        }

        @Override
        public String placeOrder(String id) {
            if (id.equals("BAD")) {
                throw new IllegalStateException("card declined");
            }
            events.seen.add("place " + id);
            return "placed " + id;
        }

        @Override
        public int importOrders(String file) throws java.io.IOException {
            events.seen.add("import " + file);
            throw new java.io.IOException("cannot read " + file);
        }

        @Override
        public String describe(String id) {
            events.seen.add("describe " + id);
            return "order " + id;
        }
    }

    private static OrderService proxied(Events events) {
        return Transactions.wrap(new Orders(events), OrderService.class, events);
    }

    @Test
    void aMarkedMethodRunsInATransaction() {
        Events events = new Events();

        String result = proxied(events).placeOrder("A-1");

        assertThat(result).isEqualTo("placed A-1");
        assertThat(events.seen).containsExactly("begin", "place A-1", "commit");
    }

    @Test
    void aFailureRollsBackAndIsNotCommitted() {
        Events events = new Events();
        OrderService orders = proxied(events);

        assertThatThrownBy(() -> orders.placeOrder("BAD"))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("card declined");
        assertThat(events.seen).containsExactly("begin", "rollback");

        events.seen.clear();
        assertThatThrownBy(() -> orders.importOrders("orders.csv"))
                .isExactlyInstanceOf(java.io.IOException.class)
                .hasMessage("cannot read orders.csv");
        assertThat(events.seen).containsExactly("begin", "import orders.csv", "rollback");
    }

    @Test
    void anUnmarkedMethodRunsWithoutATransaction() {
        Events events = new Events();

        assertThat(proxied(events).describe("A-1")).isEqualTo("order A-1");
        assertThat(events.seen).containsExactly("describe A-1");
    }
}
