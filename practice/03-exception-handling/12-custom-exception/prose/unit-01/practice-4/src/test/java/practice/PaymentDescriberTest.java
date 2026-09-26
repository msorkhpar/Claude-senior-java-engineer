package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

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

    @Test
    void everyMemberIsFinal() {
        assertThat(PaymentDescriber.describe(new FraudSuspectedException("f"))).isEqualTo("blocked");
        assertThat(Modifier.isFinal(CardDeclinedException.class.getModifiers())).isTrue();
        assertThat(Modifier.isFinal(PaymentTimeoutException.class.getModifiers())).isTrue();
        assertThat(Modifier.isFinal(FraudSuspectedException.class.getModifiers())).isTrue();
    }
}
