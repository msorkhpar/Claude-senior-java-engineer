package practice;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A customer's two balances, in cents. A transfer moves money from checking to savings, and the
 * fraud monitor reads total() and statement() from its own threads while transfers run.
 */
public final class Wallet {

    /** Both balances, as one reading. */
    public record Statement(long checking, long savings) {}

    private final Runnable midTransfer;      // runs between the debit and the credit; () -> {} in production
    private final AtomicLong checking;
    private final AtomicLong savings;

    public Wallet(long checking, long savings, Runnable midTransfer) {
        this.checking = new AtomicLong(checking);
        this.savings = new AtomicLong(savings);
        this.midTransfer = midTransfer;
    }

    /** Moves amount from checking to savings, running midTransfer after the debit and before the credit. */
    public void transfer(long amount) {
        checking.addAndGet(-amount);
        midTransfer.run();
        savings.addAndGet(amount);
    }

    /** Checking plus savings: what the customer owns. */
    public long total() {
        return checking.get() + savings.get();
    }

    /** Both balances, as one reading. */
    public Statement statement() {
        return new Statement(checking.get(), savings.get());
    }
}
