package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankAccountTest {

    @Test
    void depositsAndWithdraws() {
        BankAccount account = new BankAccount("123456", 1000.0);
        assertThat(account.getAccountNumber()).isEqualTo("123456");
        account.deposit(500.0);
        assertThat(account.getBalance()).isEqualTo(1500.0);
        account.withdraw(300.0);
        assertThat(account.getBalance()).isEqualTo(1200.0);
    }

    @Test
    void aBlankAccountNumberIsRefused() {
        for (String number : new String[] {null, "", "  "}) {
            assertThatThrownBy(() -> new BankAccount(number, 1000.0))
                    .as("account number %s", number)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account number cannot be null or empty");
        }
    }

    @Test
    void aNegativeOpeningBalanceIsRefused() {
        assertThatThrownBy(() -> new BankAccount("123456", -100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Initial balance cannot be negative");
        assertThat(new BankAccount("123456", 0.0).getBalance()).isZero();
    }

    @Test
    void aZeroAmountIsRefused() {
        BankAccount account = new BankAccount("123456", 1000.0);
        assertThatThrownBy(() -> account.deposit(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deposit amount must be positive");
        assertThatThrownBy(() -> account.withdraw(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Withdrawal amount must be positive");
        assertThatThrownBy(() -> account.withdraw(-100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Withdrawal amount must be positive");
        assertThat(account.getBalance()).isEqualTo(1000.0);
    }

    @Test
    void anOverdraftIsRefusedAndChangesNothing() {
        BankAccount account = new BankAccount("123456", 1000.0);
        assertThatThrownBy(() -> account.withdraw(1500.0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient funds");
        assertThat(account.getBalance()).isEqualTo(1000.0);
        account.withdraw(1000.0);
        assertThat(account.getBalance()).isZero();
    }
}
