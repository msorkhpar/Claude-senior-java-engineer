package practice;
import java.util.*;
public final class Ledger {
    private Ledger() {}
    public static void addInterest(Map<String, Integer> b, int percent) {
        for (Map.Entry<String, Integer> e : b.entrySet()) e.setValue(e.getValue() + e.getValue() * percent / 100);
    }
    public static void dropBelow(Map<String, Integer> b, int minimum) { b.values().removeIf(v -> v < minimum); }
    public static void prefixKeys(Map<String, Integer> b, String prefix) {
        for (String k : new ArrayList<>(b.keySet())) b.put(prefix + k, b.remove(k));
    }
}
