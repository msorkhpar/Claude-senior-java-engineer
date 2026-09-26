package practice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PriceLookup {

    private PriceLookup() {
    }

    /** Each code's price in list order, null for an unknown code; the list is unmodifiable. */
    public static List<Integer> pricesOf(List<String> codes, Map<String, Integer> prices) {
        return codes.stream().map(prices::get).collect(Collectors.toList());
    }
}
