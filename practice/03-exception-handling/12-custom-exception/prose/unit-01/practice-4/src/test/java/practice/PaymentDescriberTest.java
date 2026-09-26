package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentDescriberTest {

    @Test
    void describesEachKindOfFailure() {
        assertThat(PaymentDescriber.describe(new CardDeclinedException("insufficient limit")))
                .isEqualTo("declined: insufficient limit");
        assertThat(PaymentDescriber.describe(new PaymentTimeoutException("gateway slow")))
                .isEqualTo("retry later");
    }

    @Test
    void aSuspectedFraudIsBlocked() {
        assertThat(PaymentDescriber.describe(new FraudSuspectedException("velocity check")))
                .isEqualTo("blocked");
    }

    @Test
    void theFamilyIsSealed() {
        assertThat(PaymentDescriber.describe(new PaymentTimeoutException("t"))).isEqualTo("retry later");
        assertThat(PaymentException.class.isSealed()).isTrue();
        assertThat(PaymentException.class.getPermittedSubclasses()).containsExactlyInAnyOrder(
                CardDeclinedException.class, PaymentTimeoutException.class, FraudSuspectedException.class);
    }
}
