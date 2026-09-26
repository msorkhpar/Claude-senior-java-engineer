package practice;

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

        public PaymentAdapter(LegacyPaymentProcessor legacy) {
            throw new UnsupportedOperationException("write the constructor");
        }

        @Override
        public PaymentResult processPayment(String cardNumber, double amount) {
            throw new UnsupportedOperationException("write processPayment");
        }
    }
}
