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
            // TODO: Two amounts are equal when they have the same cents and currency.
            //       An amount is never equal to a fee.
            throw new IllegalStateException("TODO: Amount.equals");
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
            // TODO: Two fees are equal when they have the same cents, currency and reason.
            //       A fee is never equal to a plain amount.
            throw new IllegalStateException("TODO: Fee.equals");
        }

        @Override
        public String toString() {
            return super.toString() + " (" + reason + ")";
        }
    }
}
