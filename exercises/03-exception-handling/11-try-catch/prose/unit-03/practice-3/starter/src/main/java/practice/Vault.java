package practice;

public final class Vault {

    /** Starts the vault with {@code balance}. */
    public Vault(int balance) {
    }

    /** Takes {@code amount} out under the lock and returns the new balance. */
    public int withdraw(int amount) {
        throw new UnsupportedOperationException("write withdraw");
    }

    /** Returns the current balance. */
    public int balance() {
        throw new UnsupportedOperationException("write balance");
    }

    /** Returns whether the vault's lock is held. */
    public boolean isLocked() {
        throw new UnsupportedOperationException("write isLocked");
    }
}
