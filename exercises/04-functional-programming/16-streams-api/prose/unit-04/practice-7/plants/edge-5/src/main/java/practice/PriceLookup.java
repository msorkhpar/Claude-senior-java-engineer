package practice;

import java.util.*;
import java.util.stream.Collectors;

public final class PriceLookup {

    private PriceLookup() {
    }

    public static List<Integer> pricesOf(List<String> codes, Map<String, Integer> prices) {
        List<Integer> out = new ArrayList<>(codes.stream().map(prices::get).toList()) {
            @Override public boolean add(Integer e) { throw new UnsupportedOperationException(); }
            @Override public Integer set(int i, Integer e) { throw new UnsupportedOperationException(); }
            @Override public Integer remove(int i) { throw new UnsupportedOperationException(); }
        };
        return out;
    }
}
