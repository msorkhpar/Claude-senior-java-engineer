package practice;

public final class Account {

    /** The balance and the number of successful transactions, read together. */
    public record Snapshot(int balance, int transactions) {
    }

    /** Adds to the balance and counts one transaction. */
    public void deposit(int amount) {
        throw new UnsupportedOperationException("write deposit");
    }

    /** Takes from the balance and counts one transaction; false, changing nothing, when too small. */
    public boolean withdraw(int amount) {
        throw new UnsupportedOperationException("write withdraw");
    }

    /** Returns the balance and the transaction count, read in one step. */
    public Snapshot snapshot() {
        throw new UnsupportedOperationException("write snapshot");
    }
}
