package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void addsAmountsOfOneCurrency() {
        Money a = new Money(1050, "EUR");
        Money b = new Money(225, "EUR");
        Money sum = a.plus(b);
        assertThat(sum).isEqualTo(new Money(1275, "EUR"));
        assertThat(sum).isNotSameAs(a).isNotSameAs(b);
        assertThat(a).isEqualTo(new Money(1050, "EUR"));
        assertThat(b).isEqualTo(new Money(225, "EUR"));
    }

    @Test
    void differentCurrenciesAreRefused() {
        assertThatThrownBy(() -> new Money(1050, "EUR").plus(new Money(225, "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anOverflowingSumIsRefused() {
        assertThatThrownBy(() -> new Money(Long.MAX_VALUE, "EUR").plus(new Money(1, "EUR")))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void aMissingCurrencyIsRefused() {
        assertThatThrownBy(() -> new Money(100, null)).isInstanceOf(NullPointerException.class);
    }
}
