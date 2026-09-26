package practice;

public class Bank {
    private double ratePercent;

    public Bank(double ratePercent) {
        this.ratePercent = ratePercent;
    }

    public void setRate(double ratePercent) {
        this.ratePercent = ratePercent;
    }

    public Account open(double balance) {
        return new Account(balance);
    }

    public class Account {
        private static int opened;

        private final double balance;
        private final double rate = ratePercent;

        private Account(double balance) {
            this.balance = balance;
            opened++;
        }

        public static int opened() {
            return opened;
        }

        public double balance() {
            return balance;
        }

        public double yearlyInterest() {
            return balance * rate / 100;
        }
    }
}
