package practice;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDeskTest {

    private static final List<Order> ORDERS = List.of(
            new Order("O1", 150.0, "Alice"),
            new Order("O2", 75.0, "Bob"),
            new Order("O3", 200.0, "Charlie"));

    @Test
    void auditsAndStoresTheLargeOrders() {
        List<String> log = new ArrayList<>();
        List<Order> stored = new ArrayList<>();
        Order big = new Order("O9", 300.0, "Zed");
        List<Order> unsorted = List.of(big, new Order("O7", 120.0, "Yan"), big);
        OrderDesk.processLarge(unsorted, 100, OrderDesk.pipeline(log, stored));
        assertThat(stored).extracting(Order::id).containsExactly("O9", "O7", "O9");
        assertThat(log).containsExactly("PROCESSED: O9 for Zed", "PROCESSED: O7 for Yan", "PROCESSED: O9 for Zed");

        List<String> auditLog = new ArrayList<>();
        List<Order> processed = new ArrayList<>();
        OrderDesk.processLarge(ORDERS, 100, OrderDesk.pipeline(auditLog, processed));
        assertThat(auditLog).containsExactly("PROCESSED: O1 for Alice", "PROCESSED: O3 for Charlie");
        assertThat(processed).extracting(Order::id).containsExactly("O1", "O3");
    }

    @Test
    void eachOrderIsAuditedBeforeItIsStored() {
        List<String> auditLog = new ArrayList<>();
        List<Integer> auditLinesSeenAtEachStore = new ArrayList<>();
        List<Order> processed = new ArrayList<>() {
            @Override
            public boolean add(Order order) {
                auditLinesSeenAtEachStore.add(auditLog.size());
                return super.add(order);
            }
        };
        OrderDesk.processLarge(ORDERS, 100, OrderDesk.pipeline(auditLog, processed));
        assertThat(auditLinesSeenAtEachStore).containsExactly(1, 2);
    }

    @Test
    void anOrderAtTheThresholdIsNotProcessed() {
        List<Order> fine = new ArrayList<>();
        OrderDesk.processLarge(List.of(new Order("O6", 100.2, "Fay")), 100.5, OrderDesk.pipeline(new ArrayList<>(), fine));
        assertThat(fine).isEmpty();

        List<String> auditLog = new ArrayList<>();
        List<Order> processed = new ArrayList<>();
        List<Order> orders = List.of(new Order("O4", 100.0, "Diana"), new Order("O5", 100.01, "Eve"));
        OrderDesk.processLarge(orders, 100, OrderDesk.pipeline(auditLog, processed));
        assertThat(processed).extracting(Order::id).containsExactly("O5");
        assertThat(auditLog).containsExactly("PROCESSED: O5 for Eve");
    }

    @Test
    void aLongListIsProcessedInListOrder() {
        List<Order> orders = IntStream.range(0, 50_000)
                .mapToObj(i -> new Order("N" + i, 500.0, "C"))
                .toList();
        List<String> auditLog = new ArrayList<>();
        List<Order> processed = new ArrayList<>();
        OrderDesk.processLarge(orders, 100, OrderDesk.pipeline(auditLog, processed));
        assertThat(processed).containsExactlyElementsOf(orders);
    }
}
