package com.github.msorkhpar.claudejavatutor.exceptions;

public class BankAccount {
    private String accountNumber;
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
        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal", amount, balance);
        }
        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}

class InvalidAmountException extends Exception {
    public InvalidAmountException(String message) {
        super(message);
    }
}

class InsufficientFundsException extends Exception {
    private final double requestedAmount;
    private final double accountBalance;

    public InsufficientFundsException(String message, double requestedAmount, double accountBalance) {
        super(message);
        this.requestedAmount = requestedAmount;
        this.accountBalance = accountBalance;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }

    public double getAccountBalance() {
        return accountBalance;
    }
}