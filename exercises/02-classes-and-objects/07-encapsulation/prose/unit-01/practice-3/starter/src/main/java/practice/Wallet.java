package practice;

public class Wallet {

    private final String owner;
    private long cents;

    private Wallet(String owner, long cents) {
        this.owner = owner;
        this.cents = cents;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Auditor auditor() {
        throw new UnsupportedOperationException("write auditor");
    }

    public void deposit(long cents) {
        throw new UnsupportedOperationException("write deposit");
    }

    public long balance() {
        throw new UnsupportedOperationException("write balance");
    }

    /** Collects a wallet's owner and opening balance, then builds it. */
    public static final class Builder {

        public Builder owner(String owner) {
            throw new UnsupportedOperationException("write owner");
        }

        public Builder cents(long cents) {
            throw new UnsupportedOperationException("write cents");
        }

        public Wallet build() {
            throw new UnsupportedOperationException("write build");
        }
    }

    /** Reads and corrects the wallet it belongs to. */
    public class Auditor {

        public String report() {
            throw new UnsupportedOperationException("write report");
        }

        public void correct(long delta) {
            throw new UnsupportedOperationException("write correct");
        }
    }
}
