package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentsTest {

    @Test
    void payAllWorksWithEitherProcessor() {
        Payments.PaymentProcessor card = new Payments.CreditCardProcessor(100);
        assertThat(Payments.payAll(card, 30, 50, 40)).isEqualTo(2);
        assertThat(card.charged()).isEqualTo(80.0);
        Payments.PaymentProcessor paypal = new Payments.PayPalProcessor(60);
        assertThat(Payments.payAll(paypal, 20, 50)).isEqualTo(1);
        assertThat(paypal.charged()).isEqualTo(20.0);
    }

    @Test
    void aPaymentExactlyAtTheLimitGoesThrough() {
        Payments.PaymentProcessor card = new Payments.CreditCardProcessor(100);
        assertThat(Payments.payAll(card, 30, 70)).isEqualTo(2);
        assertThat(card.charged()).isEqualTo(100.0);
        Payments.PaymentProcessor paypal = new Payments.PayPalProcessor(60);
        assertThat(paypal.processPayment(60)).isTrue();
    }

    @Test
    void nonPositiveAmountsAreRefused() {
        Payments.PaymentProcessor card = new Payments.CreditCardProcessor(100);
        Payments.PaymentProcessor paypal = new Payments.PayPalProcessor(60);
        assertThat(Payments.payAll(card, 0, -5)).isZero();
        assertThat(Payments.payAll(paypal, 0, -5)).isZero();
        assertThat(card.charged()).isZero();
        assertThat(paypal.charged()).isZero();
    }

    @Test
    void aRefundCannotExceedTheCharges() {
        Payments.PaymentProcessor card = new Payments.CreditCardProcessor(100);
        assertThatThrownBy(() -> card.refund(10)).isInstanceOf(IllegalArgumentException.class);
        card.processPayment(30);
        card.refund(30);
        assertThat(card.charged()).isZero();
        Payments.PaymentProcessor paypal = new Payments.PayPalProcessor(60);
        paypal.processPayment(20);
        assertThatThrownBy(() -> paypal.refund(25)).isInstanceOf(IllegalArgumentException.class);
        assertThat(paypal.charged()).isEqualTo(20.0);
    }
}
