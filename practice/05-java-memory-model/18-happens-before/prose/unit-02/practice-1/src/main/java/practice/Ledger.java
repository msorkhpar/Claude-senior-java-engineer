package practice;

public final class Ledger {

    /** Adds amount, then runs whileLocked while still holding the ledger's lock. */
    public void deposit(long amount, Runnable whileLocked) {
        throw new UnsupportedOperationException("write deposit");
    }

    /** Takes amount off the balance, or throws IllegalStateException when the balance is smaller. */
    public void withdraw(long amount) {
        throw new UnsupportedOperationException("write withdraw");
    }

    /** Returns the balance. */
    public long balance() {
        throw new UnsupportedOperationException("write balance");
    }
}
