package practice;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WebhookTest {

    private static final byte[] JEFE = "Jefe".getBytes(StandardCharsets.UTF_8);
    private static final String BODY = "what do ya want for nothing?";
    private static final String TAG = "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843";

    @Test
    @Order(1)
    void signsAndVerifiesTheRfcExample() throws Exception {
        assertThat(Webhook.sign(JEFE, BODY)).isEqualTo(TAG);
        assertThat(Webhook.verify(JEFE, BODY, new StringBuilder(TAG).toString())).isTrue();
        assertThat(Webhook.verify(JEFE, BODY + "!", TAG)).isFalse();
    }

    @Test
    @Order(2)
    void aShortenedSignatureFails() throws Exception {
        assertThat(Webhook.verify(JEFE, BODY, TAG.substring(0, 32))).isFalse();
        assertThat(Webhook.verify(JEFE, BODY, TAG.toUpperCase(Locale.ROOT))).isFalse();
        assertThat(Webhook.verify(JEFE, BODY, "")).isFalse();
    }

    @Test
    @Order(3)
    void aMalformedSignatureIsSimplyFalse() throws Exception {
        assertThat(Webhook.verify(JEFE, BODY, "not-hex!")).isFalse();
        assertThat(Webhook.verify(JEFE, BODY, TAG.substring(0, 63) + "g")).isFalse();
    }

    @Test
    @Order(4)
    void theTagDependsOnTheKey() throws Exception {
        byte[] other = "Jeff".getBytes(StandardCharsets.UTF_8);

        assertThat(Webhook.sign(other, BODY)).isNotEqualTo(TAG);
        assertThat(Webhook.verify(other, BODY, TAG)).isFalse();
    }
}
