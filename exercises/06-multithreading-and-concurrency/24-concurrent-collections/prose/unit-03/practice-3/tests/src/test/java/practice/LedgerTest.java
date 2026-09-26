package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LedgerTest {

    private static String name(String s) {
        return new String(s.toCharArray());
    }

    @Test
    void updatesInPlace() {
        Map<String, Integer> balances = new HashMap<>();
        balances.put(name("ann"), 1000);
        balances.put(name("bob"), 2000);
        Ledger.addInterest(balances, 10);
        assertThat(balances).containsExactlyInAnyOrderEntriesOf(Map.of("ann", 1100, "bob", 2200));
        Ledger.dropBelow(balances, 500);
        assertThat(balances).containsExactlyInAnyOrderEntriesOf(Map.of("ann", 1100, "bob", 2200));
        Map<String, Integer> one = new HashMap<>();
        one.put(name("ann"), 1000);
        Ledger.prefixKeys(one, "old-");
        assertThat(one).containsExactlyInAnyOrderEntriesOf(Map.of("old-ann", 1000));
        Map<String, Integer> concurrent = new ConcurrentHashMap<>(Map.of(name("cy"), 3000));
        Ledger.addInterest(concurrent, 5);
        assertThat(concurrent).containsExactlyInAnyOrderEntriesOf(Map.of("cy", 3150));
    }

    @Test
    void dropsSeveralAccounts() {
        Map<String, Integer> balances = new HashMap<>();
        balances.put(name("ann"), 1000);
        balances.put(name("bob"), 150);
        balances.put(name("cy"), 90);
        balances.put(name("dee"), 5000);
        balances.put(name("eve"), 499);
        balances.put(name("fay"), 500);
        Ledger.dropBelow(balances, 500);
        assertThat(balances).containsExactlyInAnyOrderEntriesOf(Map.of("ann", 1000, "dee", 5000, "fay", 500));
    }

    @Test
    void renamesEveryKey() {
        Map<String, Integer> balances = new HashMap<>();
        balances.put(name("ann"), 1000);
        balances.put(name("bob"), 2000);
        balances.put(name("cy"), 3000);
        Ledger.prefixKeys(balances, "old-");
        assertThat(balances).containsExactlyInAnyOrderEntriesOf(
                Map.of("old-ann", 1000, "old-bob", 2000, "old-cy", 3000));
    }
}
