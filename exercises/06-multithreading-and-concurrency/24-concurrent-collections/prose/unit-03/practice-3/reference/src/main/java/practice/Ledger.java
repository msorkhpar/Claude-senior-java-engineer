package practice;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Ledger {

    private Ledger() {
    }

    /** Adds balance * percent / 100 to every balance, in place. */
    public static void addInterest(Map<String, Integer> balances, int percent) {
        for (Map.Entry<String, Integer> e : balances.entrySet()) {
            e.setValue(e.getValue() + e.getValue() * percent / 100);
        }
    }

    /** Removes every account whose balance is below minimum, in place. */
    public static void dropBelow(Map<String, Integer> balances, int minimum) {
        balances.values().removeIf(balance -> balance < minimum);
    }

    /** Renames every account k to prefix + k, keeping its balance, in place. */
    public static void prefixKeys(Map<String, Integer> balances, String prefix) {
        Map<String, Integer> renamed = new LinkedHashMap<>();
        balances.forEach((k, v) -> renamed.put(prefix + k, v));
        balances.clear();
        balances.putAll(renamed);
    }
}
