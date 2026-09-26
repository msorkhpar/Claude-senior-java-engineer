package practice;

import java.util.List;
import java.util.Map;

public final class PriceLookup {

    private PriceLookup() {
    }

    /** Each code's price in list order, null for an unknown code; the list is unmodifiable. */
    public static List<Integer> pricesOf(List<String> codes, Map<String, Integer> prices) {
        return codes.stream()
                .map(code -> prices.entrySet().stream()
                        .filter(entry -> entry.getKey() == code)
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(null))
                .toList();
    }
}
