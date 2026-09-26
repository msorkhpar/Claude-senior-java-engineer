package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerTest {

    @Test
    void depositsAndWithdrawalsMakeTheBalance() {
        List<Ledger.Transaction> txs = List.of(
                new Ledger.Deposit("A", 100),
                new Ledger.Withdrawal("A", 30),
                new Ledger.Deposit("A", 50));
        assertThat(Ledger.balance("A", txs)).isEqualTo(120);
        assertThat(Ledger.balance("A", List.of())).isZero();
    }

    @Test
    void otherAccountsAreIgnored() {
        List<Ledger.Transaction> txs = List.of(
                new Ledger.Deposit("A", 100),
                new Ledger.Deposit("B", 999),
                new Ledger.Withdrawal("B", 5));
        assertThat(Ledger.balance("A", txs)).isEqualTo(100);
    }

    @Test
    void aTransferMovesMoneyBothWays() {
        List<Ledger.Transaction> txs = List.of(
                new Ledger.Deposit("A", 100),
                new Ledger.Transfer("A", "B", 40));
        assertThat(Ledger.balance("A", txs)).isEqualTo(60);
        assertThat(Ledger.balance("B", txs)).isEqualTo(40);
    }

    @Test
    void aTransferToItselfChangesNothing() {
        List<Ledger.Transaction> txs = List.of(
                new Ledger.Deposit("A", 100),
                new Ledger.Transfer("A", "A", 40));
        assertThat(Ledger.balance("A", txs)).isEqualTo(100);
    }

    @Test
    void accountsMatchByNameNotByObject() {
        String account = new String("A");
        List<Ledger.Transaction> txs = List.of(
                new Ledger.Deposit(new String("A"), 100),
                new Ledger.Withdrawal(new String("A"), 30),
                new Ledger.Transfer(new String("A"), new String("B"), 20));
        assertThat(Ledger.balance(account, txs)).isEqualTo(50);
        assertThat(Ledger.balance(new String("B"), txs)).isEqualTo(20);
    }
}
