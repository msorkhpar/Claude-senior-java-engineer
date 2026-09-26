package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Inventory {
    private final Map<String, Integer> stock = new TreeMap<>();

    public Inventory() {
    }

    public void add(String item, int quantity) {
        stock.merge(item, quantity, Integer::sum);
    }

    public void remove(String item, int quantity) {
        int held = quantityOf(item);
        if (held - quantity <= 0) {
            stock.remove(item);
        } else {
            stock.put(item, held - quantity);
        }
    }

    public int quantityOf(String item) {
        return stock.getOrDefault(item, 0);
    }

    public List<String> items() {
        return new ArrayList<>(stock.keySet());
    }
}
