package practice;

import java.util.Objects;

public final class Payments {

    private Payments() {
    }

    /** The interface the new code expects. */
    public interface ModernPaymentGateway {
        PaymentResult processPayment(String cardNumber, double amount);
    }

    public record PaymentResult(boolean success, int statusCode) {
    }

    /** The existing processor: it charges whole cents and returns a status code (0 = success). */
    public static class LegacyPaymentProcessor {
        public int charge(String card, int amountInCents) {
            return 0;
        }
    }

    public static final class PaymentAdapter implements ModernPaymentGateway {

        private final LegacyPaymentProcessor legacy;

        public PaymentAdapter(LegacyPaymentProcessor legacy) {
            this.legacy = Objects.requireNonNull(legacy, "legacy processor must not be null");
        }

        @Override
        public PaymentResult processPayment(String cardNumber, double amount) {
            int cents = (int) Math.round(amount * 100);
            int status = legacy.charge(cardNumber, cents);
            return new PaymentResult(status == 0, status);
        }
    }
}
