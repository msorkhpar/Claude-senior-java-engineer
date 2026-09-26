package practice;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import practice.Payments.CreditCard;
import practice.Payments.Crypto;
import practice.Payments.PayPal;
import practice.Payments.PaymentResult;
import practice.Payments.PaymentStrategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentsTest {

    private static final String CARD = "0000111122224242";

    @Test
    void eachStrategyPaysAndDescribesItself() {
        PaymentStrategy card = new CreditCard(CARD, "12/30");
        PaymentStrategy payPal = new PayPal("buyer@example.org");
        PaymentStrategy crypto = new Crypto("wallet-01");

        assertThat(card.pay(12.5)).isEqualTo(new PaymentResult(true, "Paid 12.50 via credit card ending in 4242"));
        assertThat(payPal.pay(3)).isEqualTo(new PaymentResult(true, "Paid 3.00 via PayPal (buyer@example.org)"));
        assertThat(crypto.pay(7.25)).isEqualTo(new PaymentResult(true, "Paid 7.25 via crypto wallet"));
        assertThat(card.pay(0.01)).isEqualTo(new PaymentResult(true, "Paid 0.01 via credit card ending in 4242"));
        assertThat(Payments.describe(card)).isEqualTo("Credit card ending in 4242");
        assertThat(Payments.describe(payPal)).isEqualTo("PayPal account: buyer@example.org");
        assertThat(Payments.describe(crypto)).isEqualTo("Crypto wallet: wallet-01");
    }

    @Test
    void zeroIsNotAPositiveAmount() {
        PaymentResult refused = new PaymentResult(false, "Amount must be positive");

        assertThat(new CreditCard(CARD, "12/30").pay(0)).isEqualTo(refused);
        assertThat(new PayPal("buyer@example.org").pay(0)).isEqualTo(refused);
        assertThat(new Crypto("wallet-01").pay(0)).isEqualTo(refused);
        assertThat(new Crypto("wallet-01").pay(-5)).isEqualTo(refused);
        assertThat(new Crypto("wallet-01").pay(Double.NaN)).isEqualTo(refused);
    }

    @Test
    void blankDetailsAreRefused() {
        assertThatThrownBy(() -> new CreditCard("   ", "12/30")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CreditCard(CARD, " ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PayPal("\t")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Crypto("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PayPal(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Crypto("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void amountsReadTheSameInEveryLocale() {
        Locale before = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);

            assertThat(new CreditCard(CARD, "12/30").pay(12.5).message())
                    .isEqualTo("Paid 12.50 via credit card ending in 4242");
            assertThat(new Crypto("wallet-01").pay(1234.5).message()).isEqualTo("Paid 1234.50 via crypto wallet");
        } finally {
            Locale.setDefault(before);
        }
    }
}
