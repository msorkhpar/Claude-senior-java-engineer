package practice;

/** Checked: an amount that is not positive. */
class InvalidAmountException extends Exception {
    InvalidAmountException(String message) {
        super(message);
    }
}

/** Checked: a withdrawal larger than the balance. Carries both numbers. */
class InsufficientFundsException extends Exception {

    InsufficientFundsException(String message, double requestedAmount, double accountBalance) {
        throw new UnsupportedOperationException("write InsufficientFundsException(String, double, double)");
    }

    double getRequestedAmount() {
        throw new UnsupportedOperationException("write getRequestedAmount");
    }

    double getAccountBalance() {
        throw new UnsupportedOperationException("write getAccountBalance");
    }
}

public class BankAccount {

    public BankAccount(String accountNumber, double initialBalance) {
    }

    public void deposit(double amount) throws InvalidAmountException {
        throw new UnsupportedOperationException("write deposit");
    }

    public void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        throw new UnsupportedOperationException("write withdraw");
    }

    public double getBalance() {
        throw new UnsupportedOperationException("write getBalance");
    }
}
