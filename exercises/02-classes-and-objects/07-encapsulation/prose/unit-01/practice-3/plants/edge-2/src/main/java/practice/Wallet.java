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
        return new Auditor();
    }

    public void deposit(long cents) {
        this.cents += cents;
    }

    public long balance() {
        return cents;
    }

    /** Collects a wallet's owner and opening balance, then builds it. */
    public static final class Builder {

        private String owner;
        private long cents;

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder cents(long cents) {
            this.cents = cents;
            return this;
        }

        public Wallet build() {
            return new Wallet(owner, cents);
        }
    }

    /** Reads and corrects the wallet it belongs to. */
    public class Auditor {

        public String report() {
            return owner + ": " + cents;
        }

        private long corrected = cents;

        public void correct(long delta) {
            corrected += delta;
        }
    }
}
