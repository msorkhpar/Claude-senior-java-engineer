package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import practice.Ledger.Deposit;
import practice.Ledger.Entry;
import practice.Ledger.Fee;
import practice.Ledger.Withdrawal;

class LedgerTest {

    @Test
    void balanceAddsDepositsAndSubtractsWithdrawalsAndFees() {
        List<Entry> month = List.of(new Deposit(250_000), new Withdrawal(61_075), new Fee(399, "wire transfer"),
                new Deposit(1_205), new Withdrawal(190_000));
        assertThat(Ledger.balanceCents(month)).isEqualTo(250_000 - 61_075 - 399 + 1_205 - 190_000);
        assertThat(Ledger.balanceCents(List.of())).isZero();
    }

    @Test
    void statementPrintsEachKindOfEntry() {
        assertThat(Ledger.statementLine(new Deposit(1_205))).isEqualTo("+12.05 deposit");
        assertThat(Ledger.statementLine(new Withdrawal(190_000))).isEqualTo("-1900.00 withdrawal");
        assertThat(Ledger.statementLine(new Fee(7, "paper statement"))).isEqualTo("-0.07 fee: paper statement");
    }

    @Test
    void theEntryKindsAreAFixedSetTheCompilerKnows() {
        assertThat(Entry.class.isSealed()).isTrue();
        assertThat(Arrays.stream(Entry.class.getPermittedSubclasses()).map(Class::getSimpleName))
                .containsExactlyInAnyOrder("Deposit", "Withdrawal", "Fee");
    }
}
