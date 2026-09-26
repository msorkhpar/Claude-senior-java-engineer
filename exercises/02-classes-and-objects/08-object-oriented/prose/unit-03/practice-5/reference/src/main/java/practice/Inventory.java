package practice;

import java.util.Map;
import java.util.TreeMap;

public class Inventory {
    private final Map<String, Integer> stock = new TreeMap<>();

    public void add(String item, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("a quantity must be positive");
        }
        stock.merge(item, quantity, Integer::sum);
    }

    public boolean remove(String item, int quantity) {
        int have = count(item);
        if (quantity > have) {
            return false;
        }
        if (quantity == have) {
            stock.remove(item);
        } else {
            stock.put(item, have - quantity);
        }
        return true;
    }

    public int count(String item) {
        return stock.getOrDefault(item, 0);
    }

    public Map<String, Integer> items() {
        return Map.copyOf(stock);
    }
}
