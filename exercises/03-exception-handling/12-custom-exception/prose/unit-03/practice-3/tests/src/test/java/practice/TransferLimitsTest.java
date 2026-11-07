package practice;

import org.junit.jupiter.api.Test;

import practice.TransferLimits.TransferRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class TransferLimitsTest {

    private static final TransferLimits LIMITS = new TransferLimits(100, 2_500_000);

    private static TransferRequest request(String id, long cents) {
        return new TransferRequest(id, "DE89370400440532013000", "GB29NWBK60161331926819",
                "tok_test_" + id.hashCode() + "_sEcReT", cents);
    }

    private static String messageFor(TransferRequest r) {
        IllegalArgumentException e = catchThrowableOfType(() -> LIMITS.check(r), IllegalArgumentException.class);
        assertThat(e).as("transfer of %d cents should be rejected", r.amountCents()).isNotNull();
        return e.getMessage();
    }

    @Test
    void onlyAmountsOutsideTheLimitsAreRejected() {
        assertThatCode(() -> LIMITS.check(request("tr-a1", 100))).doesNotThrowAnyException();
        assertThatCode(() -> LIMITS.check(request("tr-a2", 2_500_000))).doesNotThrowAnyException();
        assertThatThrownBy(() -> LIMITS.check(request("tr-a3", 0))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aRejectionNamesTheAmountAndTheTransfer() {
        assertThat(messageFor(request("tr-8841", 99))).contains("99", "tr-8841");
        assertThat(messageFor(request("tr-8842", 2_500_001))).contains("2500001", "tr-8842");
    }

    @Test
    void aRejectionNamesTheAmountsThatWouldHaveBeenAccepted() {
        assertThat(messageFor(request("tr-8843", 7))).contains("100", "2500000");
    }

    @Test
    void aRejectionCarriesNoCredentialsOrAccountNumbers() {
        TransferRequest r = request("tr-8844", 3_000_000);

        assertThat(messageFor(r)).doesNotContain(r.apiToken(), r.fromIban(), r.toIban());
    }
}
