package practice;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** The daily sales report a bakery chain sends to its city managers. */
public class CityReport {

    public record Sale(String city, String customer, long cents) {}

    /** The report, one line per city in alphabetical order, then the large and small sales. */
    public static String render(List<Sale> sales, long largeFromCents) {
        StringBuilder out = new StringBuilder();
        Map<String, Integer> customers = customersByCity(sales);
        revenueByCity(sales).forEach((city, cents) -> out.append(city).append(": ").append(cents)
                .append(" cents from ").append(customers.get(city)).append(" customers\n"));
        Map<Boolean, List<Sale>> bySize = splitBySize(sales, largeFromCents);
        out.append("large sales: ").append(bySize.get(true).size())
                .append(", small sales: ").append(bySize.get(false).size()).append('\n');
        return out.toString();
    }

    /** Revenue per city. The report prints the cities in the order this map iterates them. */
    static Map<String, Long> revenueByCity(List<Sale> sales) {
        return sales.stream().collect(Collectors.groupingBy(Sale::city, TreeMap::new,
                Collectors.summingLong(Sale::cents)));
    }

    /** How many different customers bought in each city. */
    static Map<String, Integer> customersByCity(List<Sale> sales) {
        return sales.stream().collect(Collectors.groupingBy(Sale::city,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    /** The sales of at least largeFromCents under true, the others under false. */
    static Map<Boolean, List<Sale>> splitBySize(List<Sale> sales, long largeFromCents) {
        return sales.stream().collect(Collectors.partitioningBy(s -> s.cents() >= largeFromCents));
    }
}
