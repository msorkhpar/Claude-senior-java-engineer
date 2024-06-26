package com.github.msorkhpar.claudejavatutor.encapsulation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class BankAccountTest {

    @Test
    void testConstructorWithValidInput() {
        BankAccount account = new BankAccount("123456", 1000.0);
        assertThat(account.getAccountNumber()).isEqualTo("123456");
        assertThat(account.getBalance()).isEqualTo(1000.0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    void testConstructorWithInvalidAccountNumber(String invalidAccountNumber) {
        assertThatThrownBy(() -> new BankAccount(invalidAccountNumber, 1000.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account number cannot be null or empty");
    }

    @Test
    void testConstructorWithNegativeInitialBalance() {
        assertThatThrownBy(() -> new BankAccount("123456", -100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Initial balance cannot be negative");
    }

    @Test
    void testDeposit() {
        BankAccount account = new BankAccount("123456", 1000.0);
        account.deposit(500.0);
        assertThat(account.getBalance()).isEqualTo(1500.0);
    }

    @Test
    void testDepositNegativeAmount() {
        BankAccount account = new BankAccount("123456", 1000.0);
        assertThatThrownBy(() -> account.deposit(-100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deposit amount must be positive");
    }

    @Test
    void testWithdraw() {
        BankAccount account = new BankAccount("123456", 1000.0);
        account.withdraw(500.0);
        assertThat(account.getBalance()).isEqualTo(500.0);
    }

    @Test
    void testWithdrawNegativeAmount() {
        BankAccount account = new BankAccount("123456", 1000.0);
        assertThatThrownBy(() -> account.withdraw(-100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Withdrawal amount must be positive");
    }

    @Test
    void testWithdrawInsufficientFunds() {
        BankAccount account = new BankAccount("123456", 1000.0);
        assertThatThrownBy(() -> account.withdraw(1500.0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void testToString() {
        BankAccount account = new BankAccount("123456", 1000.0);
        assertThat(account.toString()).contains("accountNumber='123456'", "balance=1000.0");
    }
}