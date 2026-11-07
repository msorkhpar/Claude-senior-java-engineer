package practice;

import java.util.function.BooleanSupplier;

/** Sending payments to the bank, with retries where they are safe. */
public class Retries {

    /** How many times an idempotent operation may be tried. The platform's configuration sets it. */
    public static int idempotentAttempts = 4;

    /** The platform team's interface for operations that are safe to repeat. */
    public interface Idempotent {
        /** The key the bank uses to recognise a repeat of the same operation. */
        String key();

        default int maxAttempts() { return idempotentAttempts; }
    }

    /** A money movement. A charge sent twice charges twice, so by default a payment is tried once. */
    public static class Payment {
        private final long cents;

        public Payment(long cents) { this.cents = cents; }

        public long cents() { return cents; }

        public int maxAttempts() { return 1; }
    }

    public static class Charge extends Payment {
        public Charge(long cents) { super(cents); }
    }

    /** A refund carries the key of the charge it reverses, so the bank applies it once however often it is sent. */
    public static class Refund extends Payment implements Idempotent {
        private final String key;

        public Refund(long cents, String chargeKey) {
            super(cents);
            this.key = "refund-" + chargeKey;
        }

        @Override public String key() { return key; }

        @Override public int maxAttempts() {
            return Idempotent.super.maxAttempts();
        }
    }

    /**
     * Sends a payment: makes one attempt after another until one succeeds or the payment has used
     * every attempt it is allowed. Returns whether an attempt succeeded.
     */
    public static boolean send(Payment payment, BooleanSupplier attempt) {
        for (int i = 0; i < payment.maxAttempts(); i++) {
            if (attempt.getAsBoolean()) return true;
        }
        return false;
    }
}
