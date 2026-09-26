package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class PaymentsTest {

    private static final String CARD = "4111111111111111";

    @Test
    void approvesWithinTheLimitAndDeclinesOverIt() throws Exception {
        assertThat(Payments.authorize(CARD, 2000, 3000)).isEqualTo("approved");
        CardDeclinedException e = catchThrowableOfType(
                () -> Payments.authorize(CARD, 5000, 3000), CardDeclinedException.class);
        assertThat(e).isNotNull();
        assertThat(e.getLastFour()).isEqualTo("1111");
        assertThat(e.getMessage()).contains("declined").contains("5000").contains("3000");
    }

    @Test
    void theMessageNeverHoldsTheCardNumber() {
        CardDeclinedException e = catchThrowableOfType(
                () -> Payments.authorize("5500005555555559", 9000, 100), CardDeclinedException.class);
        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("Card ending 5559 declined: 9000 over limit 100");
        assertThat(e.toString()).doesNotContain("5500005555555559");
    }

    @Test
    void anAmountAtTheLimitIsApproved() throws Exception {
        assertThat(Payments.authorize(CARD, 3000, 3000)).isEqualTo("approved");
    }
}
