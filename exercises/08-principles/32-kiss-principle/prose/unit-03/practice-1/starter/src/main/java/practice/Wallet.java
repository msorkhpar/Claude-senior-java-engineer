package practice;

/**
 * A customer's two balances, in cents. A transfer moves money from checking to savings, and the
 * fraud monitor reads total() and statement() from its own threads while transfers run.
 */
public final class Wallet {

    /** Both balances, as one reading. */
    public record Statement(long checking, long savings) {}

    private final Runnable midTransfer;      // runs between the debit and the credit; () -> {} in production
    private long checking;
    private long savings;

    public Wallet(long checking, long savings, Runnable midTransfer) {
        this.checking = checking;
        this.savings = savings;
        this.midTransfer = midTransfer;
    }

    /** Moves amount from checking to savings, running midTransfer after the debit and before the credit. */
    public void transfer(long amount) {
        // TODO: debit checking, run midTransfer, credit savings, safely while other threads use this wallet.
        throw new IllegalStateException("TODO: transfer");
    }

    /** Checking plus savings: what the customer owns. */
    public long total() {
        // TODO: what the customer owns, as one reading, whatever transfer is in progress.
        throw new IllegalStateException("TODO: total");
    }

    /** Both balances, as one reading. */
    public Statement statement() {
        // TODO: both balances as they stood at one moment.
        throw new IllegalStateException("TODO: statement");
    }
}
