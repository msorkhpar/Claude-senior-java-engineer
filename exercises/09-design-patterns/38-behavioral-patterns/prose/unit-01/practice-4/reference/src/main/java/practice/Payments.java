package practice;

import java.util.Locale;

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
            requireDetail(cardNumber, "Card number");
            requireDetail(expiryDate, "Expiry date");
        }

        @Override
        public PaymentResult pay(double amount) {
            return paid(amount, "via credit card ending in " + lastFour(cardNumber));
        }
    }

    public record PayPal(String email) implements PaymentStrategy {
        public PayPal {
            requireDetail(email, "Email");
        }

        @Override
        public PaymentResult pay(double amount) {
            return paid(amount, "via PayPal (" + email + ")");
        }
    }

    public record Crypto(String wallet) implements PaymentStrategy {
        public Crypto {
            requireDetail(wallet, "Wallet");
        }

        @Override
        public PaymentResult pay(double amount) {
            return paid(amount, "via crypto wallet");
        }
    }

    public static String describe(PaymentStrategy strategy) {
        return switch (strategy) {
            case CreditCard card -> "Credit card ending in " + lastFour(card.cardNumber());
            case PayPal payPal -> "PayPal account: " + payPal.email();
            case Crypto crypto -> "Crypto wallet: " + crypto.wallet();
        };
    }

    private static void requireDetail(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(what + " cannot be null or blank");
        }
    }

    private static PaymentResult paid(double amount, String how) {
        if (!(amount > 0)) {
            return new PaymentResult(false, "Amount must be positive");
        }
        return new PaymentResult(true, String.format(Locale.ROOT, "Paid %.2f %s", amount, how));
    }

    private static String lastFour(String cardNumber) {
        return cardNumber.substring(cardNumber.length() - 4);
    }
}
