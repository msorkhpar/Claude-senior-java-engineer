package practice;

public class Bank {

    public Bank(double ratePercent) {
        throw new UnsupportedOperationException("write the constructor");
    }

    public void setRate(double ratePercent) {
        throw new UnsupportedOperationException("write setRate");
    }

    public Account open(double balance) {
        throw new UnsupportedOperationException("write open");
    }

    public class Account {

        public static int opened() {
            throw new UnsupportedOperationException("write opened");
        }

        public double balance() {
            throw new UnsupportedOperationException("write balance");
        }

        public double yearlyInterest() {
            throw new UnsupportedOperationException("write yearlyInterest");
        }
    }
}
