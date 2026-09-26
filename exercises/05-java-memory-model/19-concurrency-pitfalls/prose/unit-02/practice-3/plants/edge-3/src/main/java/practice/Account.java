package practice;

public final class Account {

    /** The balance and the number of successful transactions, read together. */
    public record Snapshot(int balance, int transactions) {
    }

    private int balance;
    private int transactions;

    /** Adds to the balance and counts one transaction. */
    public synchronized void deposit(int amount) {
        balance += amount;
        transactions++;
    }

    /** Takes from the balance and counts one transaction; false, changing nothing, when too small. */
    public boolean withdraw(int amount) {
        if (balance < amount) {
            return false;
        }
        balance -= amount;
        transactions++;
        return true;
    }

    /** Returns the balance and the transaction count, read in one step. */
    public synchronized Snapshot snapshot() {
        return new Snapshot(balance, transactions);
    }
}
