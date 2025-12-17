package com.github.msorkhpar.claudejavatutor.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BankAccountTest {

    private BankAccount account;

    @BeforeEach
    void setUp() {
        account = new BankAccount("123456", 1000.0);
    }

    @Test
    void testDeposit_ValidAmount() throws InvalidAmountException {
        account.deposit(500.0);
        assertThat(account.getBalance()).isEqualTo(1500.0);
    }

    @Test
    void testDeposit_InvalidAmount() {
        assertThatThrownBy(() -> account.deposit(-100.0))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Deposit amount must be positive");
    }

    @Test
    void testWithdraw_ValidAmount() throws InvalidAmountException, InsufficientFundsException {
        account.withdraw(500.0);
        assertThat(account.getBalance()).isEqualTo(500.0);
    }

    @Test
    void testWithdraw_InvalidAmount() {
        assertThatThrownBy(() -> account.withdraw(-100.0))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Withdrawal amount must be positive");
    }

    @Test
    void testWithdraw_InsufficientFunds() {
        assertThatThrownBy(() -> account.withdraw(1500.0))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds for withdrawal")
                .satisfies(exception -> {
                    InsufficientFundsException e = (InsufficientFundsException) exception;
                    assertThat(e.getRequestedAmount()).isEqualTo(1500.0);
                    assertThat(e.getAccountBalance()).isEqualTo(1000.0);
                });
    }
}