package practice;

public final class Payments {

    private Payments() {
    }

    public record PaymentResult(boolean success, String message) {
    }

    public sealed interface PaymentStrategy permits CreditCard, PayPal, Crypto {
        PaymentResult pay(double amount);
    }

    public record CreditCard(String cardNumber, String expiryDate) implements PaymentStrategy {
        public CreditCard {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public PaymentResult pay(double amount) {
            throw new UnsupportedOperationException("TODO");
        }
    }

    public record PayPal(String email) implements PaymentStrategy {
        public PayPal {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public PaymentResult pay(double amount) {
            throw new UnsupportedOperationException("TODO");
        }
    }

    public record Crypto(String wallet) implements PaymentStrategy {
        public Crypto {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public PaymentResult pay(double amount) {
            throw new UnsupportedOperationException("TODO");
        }
    }

    public static String describe(PaymentStrategy strategy) {
        throw new UnsupportedOperationException("TODO");
    }
}
