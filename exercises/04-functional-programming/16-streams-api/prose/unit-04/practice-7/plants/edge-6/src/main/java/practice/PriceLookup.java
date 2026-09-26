package practice;

import java.util.*;
import java.util.stream.Collectors;

public final class PriceLookup {

    private PriceLookup() {
    }

    public static List<Integer> pricesOf(List<String> codes, Map<String, Integer> prices) {
        return new AbstractList<Integer>() {
            @Override public Integer get(int i) { return prices.get(codes.get(i)); }
            @Override public int size() { return codes.size(); }
        };
    }
}
