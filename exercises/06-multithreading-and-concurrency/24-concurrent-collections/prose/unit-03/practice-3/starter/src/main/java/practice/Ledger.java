package practice;

import java.util.Map;

public final class Ledger {

    private Ledger() {
    }

    /** Adds balance * percent / 100 to every balance, in place. */
    public static void addInterest(Map<String, Integer> balances, int percent) {
        throw new UnsupportedOperationException("write addInterest");
    }

    /** Removes every account whose balance is below minimum, in place. */
    public static void dropBelow(Map<String, Integer> balances, int minimum) {
        throw new UnsupportedOperationException("write dropBelow");
    }

    /** Renames every account k to prefix + k, keeping its balance, in place. */
    public static void prefixKeys(Map<String, Integer> balances, String prefix) {
        throw new UnsupportedOperationException("write prefixKeys");
    }
}
