package practice;

public class Payments {

    public interface PaymentProcessor {
        boolean processPayment(double amount);

        void refund(double amount);

        double charged();
    }

    public static class CreditCardProcessor implements PaymentProcessor {
        public CreditCardProcessor(double limit) {
            throw new UnsupportedOperationException("write the CreditCardProcessor constructor");
        }

        @Override
        public boolean processPayment(double amount) {
            throw new UnsupportedOperationException("write CreditCardProcessor.processPayment");
        }

        @Override
        public void refund(double amount) {
            throw new UnsupportedOperationException("write CreditCardProcessor.refund");
        }

        @Override
        public double charged() {
            throw new UnsupportedOperationException("write CreditCardProcessor.charged");
        }
    }

    public static class PayPalProcessor implements PaymentProcessor {
        public PayPalProcessor(double balance) {
            throw new UnsupportedOperationException("write the PayPalProcessor constructor");
        }

        @Override
        public boolean processPayment(double amount) {
            throw new UnsupportedOperationException("write PayPalProcessor.processPayment");
        }

        @Override
        public void refund(double amount) {
            throw new UnsupportedOperationException("write PayPalProcessor.refund");
        }

        @Override
        public double charged() {
            throw new UnsupportedOperationException("write PayPalProcessor.charged");
        }
    }

    /** Tries each amount in order; returns how many payments were accepted. */
    public static int payAll(PaymentProcessor processor, double... amounts) {
        throw new UnsupportedOperationException("write payAll");
    }
}
