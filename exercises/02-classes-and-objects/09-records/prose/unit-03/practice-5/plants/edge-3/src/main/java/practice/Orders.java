package practice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class Orders {

    public record Order(String customer, long cents) {}

    public record CustomerTotal(String customer, long totalCents, int orders) {}

    private Orders() {
    }

    /** One total per customer, largest total first, equal totals by name. */
    public static List<CustomerTotal> totals(List<Order> orders) {
        if (orders.isEmpty()) {
            return null;
        }
        Map<String, CustomerTotal> byCustomer = new TreeMap<>();
        for (Order order : orders) {
            byCustomer.merge(order.customer(), new CustomerTotal(order.customer(), order.cents(), 1),
                    (a, b) -> new CustomerTotal(a.customer(), a.totalCents() + b.totalCents(), a.orders() + b.orders()));
        }
        List<CustomerTotal> result = new ArrayList<>(byCustomer.values());
        result.sort(Comparator.comparingLong(CustomerTotal::totalCents).reversed()
                .thenComparing(CustomerTotal::customer));
        return result;
    }
}
