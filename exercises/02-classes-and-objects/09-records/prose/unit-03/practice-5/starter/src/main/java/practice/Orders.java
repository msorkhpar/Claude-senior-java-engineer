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
        throw new UnsupportedOperationException("write totals");
    }
}
