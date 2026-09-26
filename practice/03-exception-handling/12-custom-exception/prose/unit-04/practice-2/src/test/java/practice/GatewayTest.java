package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class GatewayTest {

    private final Gateway gateway = new Gateway();

    @Test
    void sendsAndRejects() throws Exception {
        assertThat(gateway.send("r1", 1)).isEqualTo("sent r1");
        assertThat(gateway.send("r2", 10)).isEqualTo("sent r2");
        assertThatThrownBy(() -> gateway.send("r1", 11))
                .isInstanceOf(RateLimitedException.class)
                .hasMessage("Rate limit exceeded");
        assertThatThrownBy(() -> gateway.send(new String(""), 1))
                .isInstanceOf(BadRequestIdException.class)
                .hasMessage("Request id is required");
    }

    @Test
    void rateLimitingIsCheckedAndSaysWhenToRetry() {
        Throwable thrown = catchThrowable(() -> gateway.send("r1", 11));
        assertThat(thrown).isInstanceOf(RateLimitedException.class).isNotInstanceOf(RuntimeException.class);
        assertThat(((RateLimitedException) thrown).getRetryAfterSeconds()).isEqualTo(60);
    }

    @Test
    void aBadIdIsUnchecked() {
        Throwable thrown = catchThrowable(() -> gateway.send(new String(""), 1));
        assertThat(thrown).isInstanceOf(BadRequestIdException.class).isInstanceOf(RuntimeException.class);
    }

    @Test
    void aBlankOrNullIdIsBad() {
        assertThatThrownBy(() -> gateway.send(new String("   "), 1))
                .isInstanceOf(BadRequestIdException.class)
                .hasMessage("Request id is required");
        assertThatThrownBy(() -> gateway.send(null, 1))
                .isInstanceOf(BadRequestIdException.class)
                .hasMessage("Request id is required");
    }
}
