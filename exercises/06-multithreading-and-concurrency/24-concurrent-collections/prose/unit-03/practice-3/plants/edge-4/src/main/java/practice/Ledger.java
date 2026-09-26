package practice;
import java.util.*;
public final class Ledger {
    private Ledger() {}
    public static void addInterest(Map<String, Integer> b, int percent) {
        b.replaceAll((k, v) -> v + (int) Math.round(v * percent / 100.0));
    }
    public static void dropBelow(Map<String, Integer> b, int minimum) { b.values().removeIf(v -> v < minimum); }
    public static void prefixKeys(Map<String, Integer> b, String prefix) {
        Map<String, Integer> r = new LinkedHashMap<>(); b.forEach((k, v) -> r.put(prefix + k, v)); b.clear(); b.putAll(r);
    }
}
