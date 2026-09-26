package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PaymentsTest {

    /** A legacy processor that records every charge and answers a fixed status. */
    static final class RecordingProcessor extends Payments.LegacyPaymentProcessor {
        final List<String> charges = new ArrayList<>();
        final int status;

        RecordingProcessor(int status) {
            this.status = status;
        }

        @Override
        public int charge(String card, int amountInCents) {
            charges.add(card + ":" + amountInCents);
            return status;
        }
    }

    @Test
    void chargesTheLegacyProcessorInCents() {
        RecordingProcessor legacy = new RecordingProcessor(0);
        Payments.ModernPaymentGateway gateway = new Payments.PaymentAdapter(legacy);

        Payments.PaymentResult result = gateway.processPayment("4111", 12.50);

        assertThat(legacy.charges).containsExactly("4111:1250");
        assertThat(result).isEqualTo(new Payments.PaymentResult(true, 0));
    }

    @Test
    void amountsAreRoundedToTheNearestCent() {
        RecordingProcessor legacy = new RecordingProcessor(0);
        Payments.ModernPaymentGateway gateway = new Payments.PaymentAdapter(legacy);

        gateway.processPayment("4111", 19.99);
        gateway.processPayment("4111", 0.29);
        gateway.processPayment("4111", 10.004);
        gateway.processPayment("4111", 1234567.89);

        assertThat(legacy.charges).containsExactly("4111:1999", "4111:29", "4111:1000", "4111:123456789");
    }

    @Test
    void aNonZeroStatusIsAFailure() {
        RecordingProcessor legacy = new RecordingProcessor(51);
        Payments.ModernPaymentGateway gateway = new Payments.PaymentAdapter(legacy);

        Payments.PaymentResult result = gateway.processPayment("4111", 5.00);

        assertThat(legacy.charges).containsExactly("4111:500");
        assertThat(result).isEqualTo(new Payments.PaymentResult(false, 51));
        assertThat(new Payments.PaymentAdapter(new RecordingProcessor(-1)).processPayment("4111", 1.00))
                .isEqualTo(new Payments.PaymentResult(false, -1));
    }

    @Test
    void aNullProcessorIsRefusedAtOnce() {
        assertThatNullPointerException().isThrownBy(() -> new Payments.PaymentAdapter(null));
    }
}
