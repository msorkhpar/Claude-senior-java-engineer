package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * The nightly settlement report. Each merchant's day is settled with its bank on the pool, all of
 * them at once, and the report has one line per merchant, in the order the merchants were given.
 */
public class SettlementReport {

    /** Settles one merchant's day with its bank and returns the amount, in cents. */
    public interface Bank {
        long settle(String merchant) throws Exception;
    }

    private final ExecutorService pool;
    private final Bank bank;

    public SettlementReport(ExecutorService pool, Bank bank) {
        this.pool = pool;
        this.bank = bank;
    }

    /** Settles every merchant and returns the report's lines, one per merchant, in order. */
    public List<String> run(List<String> merchants) throws InterruptedException {
        List<Future<Long>> settlements = new ArrayList<>();
        for (String merchant : merchants) {
            settlements.add(pool.submit(() -> bank.settle(merchant)));
        }
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < merchants.size(); i++) {
            lines.add(line(merchants.get(i), settlements.get(i)));
        }
        return lines;
    }

    /**
     * The report's line for one merchant, once its settlement is over:
     * "m-114: settled 1999" when the bank settled it, or
     * "m-114: failed: SocketTimeoutException: bank-7 timed out" when the bank call threw, naming
     * what the bank call threw and its message.
     */
    String line(String merchant, Future<Long> settlement) throws InterruptedException {
        try {
            return merchant + ": settled " + settlement.get();
        } catch (ExecutionException e) {
            return merchant + ": failed: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
}
