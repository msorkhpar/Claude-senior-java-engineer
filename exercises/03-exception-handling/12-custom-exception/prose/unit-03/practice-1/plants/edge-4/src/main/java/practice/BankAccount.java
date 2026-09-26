package practice;

/** Checked: an amount that is not positive. */
class InvalidAmountException extends Exception {
    InvalidAmountException(String message) {
        super(message);
    }
}

/** Checked: a withdrawal larger than the balance. Carries both numbers. */
class InsufficientFundsException extends Exception {

    private final double requestedAmount;
    private final double accountBalance;

    InsufficientFundsException(String message, double requestedAmount, double accountBalance) {
        super(message);
        this.requestedAmount = requestedAmount;
        this.accountBalance = accountBalance;
    }

    double getRequestedAmount() {
        return requestedAmount;
    }

    double getAccountBalance() {
        return accountBalance;
    }
}

public class BankAccount {

    private final String accountNumber;
    private double balance;

    public BankAccount(String accountNumber, double initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    public void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive");
        }
        balance += amount;
    }

    public void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive");
        }
        if (amount >= balance) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal", amount, balance);
        }
        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }
}
