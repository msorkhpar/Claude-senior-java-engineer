package practice;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import practice.Ledger.Amount;
import practice.Ledger.Fee;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerTest {

    /** Builds each string at run time, so equal strings are distinct objects. */
    private static String text(String s) {
        return new String(s);
    }

    @Test
    void twoAmountsOfTheSameMoneyAreEqual() {
        Amount first = new Amount(1999, text("CHF"));
        Amount second = new Amount(1999, text("CHF"));

        assertThat(first.equals(second)).isTrue();
        assertThat(new HashSet<>(List.of(first, second))).hasSize(1);
    }

    @Test
    void amountsDifferingInCentsOrCurrencyAreNotEqual() {
        Amount amount = new Amount(1999, text("CHF"));

        assertThat(amount.equals(new Amount(2000, text("CHF")))).isFalse();
        assertThat(amount.equals(new Amount(1999, text("SEK")))).isFalse();
    }

    @Test
    void twoFeesForTheSameMoneyAndReasonAreEqual() {
        Fee first = new Fee(250, text("CHF"), text("card surcharge"));
        Fee second = new Fee(250, text("CHF"), text("card surcharge"));

        assertThat(first.equals(second)).isTrue();
        assertThat(new HashSet<>(List.of(first, second))).hasSize(1);
    }

    @Test
    void feesForDifferentReasonsAreNotEqual() {
        Fee surcharge = new Fee(250, text("CHF"), text("card surcharge"));
        Fee late = new Fee(250, text("CHF"), text("late payment"));

        assertThat(surcharge.equals(late)).isFalse();
        assertThat(late.equals(surcharge)).isFalse();
    }

    @Test
    void anAmountIsNeverEqualToAFeeOfTheSameMoney() {
        Amount refund = new Amount(250, text("CHF"));
        Fee surcharge = new Fee(250, text("CHF"), text("card surcharge"));

        assertThat(refund.equals(surcharge)).isFalse();
        assertThat(List.of(surcharge).contains(refund)).isFalse();
    }

    @Test
    void aFeeIsNeverEqualToAnAmountOfTheSameMoney() {
        Amount refund = new Amount(250, text("CHF"));
        Fee surcharge = new Fee(250, text("CHF"), text("card surcharge"));

        assertThat(surcharge.equals(refund)).isFalse();
        Set<Amount> entries = new HashSet<>();
        entries.add(refund);
        entries.add(surcharge);
        assertThat(entries).hasSize(2);
    }

    @Test
    void anAmountIsNotEqualToNull() {
        assertThat(new Amount(1999, text("CHF")).equals(null)).isFalse();
    }
}
