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
    public synchronized void transfer(long amount) {
        checking -= amount;
        midTransfer.run();
        savings += amount;
    }

    /** Checking plus savings: what the customer owns. */
    public synchronized long total() {
        return checking + savings;
    }

    /** Both balances, as one reading. */
    public synchronized Statement statement() {
        return new Statement(checking, savings);
    }
}
