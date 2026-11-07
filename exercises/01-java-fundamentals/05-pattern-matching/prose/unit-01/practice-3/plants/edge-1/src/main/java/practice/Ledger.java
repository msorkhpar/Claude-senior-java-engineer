package practice;

import java.util.Objects;

/** Entries of a payments ledger. A fee is an amount charged for a reason, such as "card surcharge". */
public class Ledger {

    public static class Amount {
        final long cents;
        final String currency;

        public Amount(long cents, String currency) {
            this.cents = cents;
            this.currency = currency;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Amount)) {
                return false;
            }
            Amount other = (Amount) o;
            return cents == other.cents && currency.equals(other.currency);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cents, currency);
        }

        @Override
        public String toString() {
            return cents + " " + currency;
        }
    }

    public static class Fee extends Amount {
        final String reason;

        public Fee(long cents, String currency, String reason) {
            super(cents, currency);
            this.reason = reason;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Fee)) {
                return false;
            }
            return super.equals(o) && reason.equals(((Fee) o).reason);
        }

        @Override
        public String toString() {
            return super.toString() + " (" + reason + ")";
        }
    }
}
