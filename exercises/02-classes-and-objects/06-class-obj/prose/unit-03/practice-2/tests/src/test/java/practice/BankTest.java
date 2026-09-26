package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class BankTest {

    @Test
    void anAccountEarnsItsBanksInterest() {
        int before = Bank.Account.opened();
        Bank bank = new Bank(2.0);
        Bank.Account account = bank.open(1000);
        assertThat(account.balance()).isCloseTo(1000, within(1e-9));
        assertThat(account.yearlyInterest()).isCloseTo(20.0, within(1e-9));
        bank.open(50);
        assertThat(Bank.Account.opened()).isEqualTo(before + 2);
    }

    @Test
    void eachAccountUsesItsOwnBanksRate() {
        Bank low = new Bank(1.0);
        Bank high = new Bank(5.0);
        Bank.Account atLow = low.open(1000);
        Bank.Account atHigh = high.open(1000);
        assertThat(atLow.yearlyInterest()).isCloseTo(10.0, within(1e-9));
        assertThat(atHigh.yearlyInterest()).isCloseTo(50.0, within(1e-9));
    }

    @Test
    void aRateChangeReachesExistingAccounts() {
        Bank bank = new Bank(2.0);
        Bank.Account account = bank.open(1000);
        bank.setRate(3.0);
        assertThat(account.yearlyInterest()).isCloseTo(30.0, within(1e-9));
    }
}
