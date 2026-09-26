package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class BankAccountTest {

    private final BankAccount account = new BankAccount("123456", 1000.0);

    @Test
    void depositsAndWithdraws() throws Exception {
        account.deposit(500.0);
        assertThat(account.getBalance()).isEqualTo(1500.0);
        account.withdraw(1000.0);
        assertThat(account.getBalance()).isEqualTo(500.0);
    }

    @Test
    void rejectsANegativeAmount() {
        assertThatThrownBy(() -> account.deposit(-100.0))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Deposit amount must be positive");
        assertThatThrownBy(() -> account.withdraw(-100.0))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Withdrawal amount must be positive");
    }

    @Test
    void insufficientFundsCarriesTheAmounts() {
        InsufficientFundsException e = catchThrowableOfType(
                () -> account.withdraw(1500.0), InsufficientFundsException.class);
        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("Insufficient funds for withdrawal");
        assertThat(e.getRequestedAmount()).isEqualTo(1500.0);
        assertThat(e.getAccountBalance()).isEqualTo(1000.0);
    }

    @Test
    void failedOperationsLeaveTheBalanceUnchanged() {
        assertThatThrownBy(() -> account.withdraw(1500.0)).isInstanceOf(InsufficientFundsException.class);
        assertThatThrownBy(() -> account.withdraw(-100.0)).isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> account.deposit(-5.0)).isInstanceOf(InvalidAmountException.class);
        assertThat(account.getBalance()).isEqualTo(1000.0);
    }

    @Test
    void zeroIsNotAValidAmount() {
        assertThatThrownBy(() -> account.deposit(0.0))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Deposit amount must be positive");
        assertThatThrownBy(() -> account.withdraw(0.0))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessage("Withdrawal amount must be positive");
    }

    @Test
    void theWholeBalanceMayBeWithdrawn() throws Exception {
        account.withdraw(1000.0);
        assertThat(account.getBalance()).isEqualTo(0.0);
    }
}
