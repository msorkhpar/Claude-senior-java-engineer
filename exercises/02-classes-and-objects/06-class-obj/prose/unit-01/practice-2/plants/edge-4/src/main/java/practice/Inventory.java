package practice;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class Inventory {
    private final Map<String, Integer> stock = new IdentityHashMap<>();

    public Inventory() {
    }

    public void add(String item, int quantity) {
        stock.merge(item, quantity, Integer::sum);
    }

    public void remove(String item, int quantity) {
        int held = quantityOf(item);
        if (quantity > held) {
            throw new IllegalArgumentException("Only " + held + " of " + item + " held");
        }
        if (held == quantity) {
            stock.remove(item);
        } else {
            stock.put(item, held - quantity);
        }
    }

    public int quantityOf(String item) {
        return stock.getOrDefault(item, 0);
    }

    public List<String> items() {
        return stock.keySet().stream().sorted().toList();
    }
}
