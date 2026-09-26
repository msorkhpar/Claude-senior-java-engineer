package practice;

public class Payments {

    public interface PaymentProcessor {
        boolean processPayment(double amount);

        void refund(double amount);

        double charged();
    }

    public static class CreditCardProcessor implements PaymentProcessor {
        private final double limit;
        private double charged;

        public CreditCardProcessor(double limit) {
            this.limit = limit;
        }

        @Override
        public boolean processPayment(double amount) {
            if (charged + amount > limit) {
                return false;
            }
            charged += amount;
            return true;
        }

        @Override
        public void refund(double amount) {
            if (amount > charged) {
                throw new IllegalArgumentException("cannot refund more than was charged");
            }
            charged -= amount;
        }

        @Override
        public double charged() {
            return charged;
        }
    }

    public static class PayPalProcessor implements PaymentProcessor {
        private double balance;
        private double charged;

        public PayPalProcessor(double balance) {
            this.balance = balance;
        }

        @Override
        public boolean processPayment(double amount) {
            if (amount > balance) {
                return false;
            }
            balance -= amount;
            charged += amount;
            return true;
        }

        @Override
        public void refund(double amount) {
            if (amount > charged) {
                throw new IllegalArgumentException("cannot refund more than was charged");
            }
            charged -= amount;
            balance += amount;
        }

        @Override
        public double charged() {
            return charged;
        }
    }

    /** Tries each amount in order; returns how many payments were accepted. */
    public static int payAll(PaymentProcessor processor, double... amounts) {
        int accepted = 0;
        for (double amount : amounts) {
            if (processor.processPayment(amount)) {
                accepted++;
            }
        }
        return accepted;
    }
}
