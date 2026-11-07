package practice;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** The nightly report of which products sold in which region. Several report threads share one instance. */
public class RegionReport {

    public record Sale(String region, String sku) {}

    /** Each rendering thread reuses its own buffer instead of allocating one per report. */
    final ThreadLocal<StringBuilder> scratch = newScratch();

    /** For each region, the SKUs sold there, sorted and without repeats. */
    public Map<String, SortedSet<String>> skusByRegion(List<Sale> sales) {
        SortedSet<String> skus = new TreeSet<>();
        return sales.stream().collect(Collectors.groupingBy(Sale::region,
                Collectors.mapping(Sale::sku, Collectors.toCollection(() -> skus))));
    }

    /** The buffers behind {@link #scratch}. */
    ThreadLocal<StringBuilder> newScratch() {
        return ThreadLocal.withInitial(StringBuilder::new);
    }

    /** One line per region: "region: sku, sku". */
    public String render(Map<String, SortedSet<String>> byRegion) {
        StringBuilder out = scratch.get();
        out.setLength(0);
        byRegion.forEach((region, skus) ->
                out.append(region).append(": ").append(String.join(", ", skus)).append('\n'));
        return out.toString();
    }
}
