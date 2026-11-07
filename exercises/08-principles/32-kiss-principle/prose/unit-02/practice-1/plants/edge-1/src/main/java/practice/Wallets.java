package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Game credits held in player wallets. Every transfer is applied by one owner thread; the
 * leaderboard and the support console read balances from their own threads.
 */
public class Wallets implements AutoCloseable {

    /** Called on the owner thread between the debit and the credit of a transfer; tests pause there. */
    public interface Seam {
        void midTransfer();
    }

    private final ExecutorService owner = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "wallets-owner");
        t.setDaemon(true);
        return t;
    });
    private final Map<String, Long> live = new HashMap<>();   // only the owner thread changes it
    private final Seam seam;
    private volatile Map<String, Long> published;

    public Wallets(Map<String, Long> opening, Seam seam) {
        this.seam = seam;
        live.putAll(opening);
        publish();
    }

    /** Moves credits between two wallets; the future says whether the transfer went through. */
    public Future<Boolean> transfer(String from, String to, long credits) {
        return owner.submit(() -> {
            long balance = live.getOrDefault(from, 0L);
            if (balance < credits) return false;
            live.put(from, balance - credits);
            seam.midTransfer();
            live.merge(to, credits, Long::sum);
            publish();
            return true;
        });
    }

    /** Every wallet's balance, for any thread. */
    public Map<String, Long> balances() {
        return Map.copyOf(live);
    }

    /** Runs after the opening balances are in place and after every transfer that changed them. */
    void publish() {
        published = Map.copyOf(live);
    }

    @Override
    public void close() {
        owner.shutdownNow();
    }
}
