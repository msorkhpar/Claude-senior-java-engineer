package practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchiveStampsTest {

    private final ArchiveStamps stamps = new ArchiveStamps();

    @Test
    void stampsTimesTheArchiveCanHold() {
        assertThat(stamps.header("inv-7", Instant.parse("2024-03-15T10:00:00Z"))).isEqualTo("inv-7@1710496800");
        assertThat(stamps.header("memo-2", Instant.parse("1969-07-20T20:17:40Z"))).isEqualTo("memo-2@-14182940");
        assertThat(stamps.header("last", Instant.parse("2038-01-19T03:14:07Z"))).isEqualTo("last@2147483647");
        assertThat(stamps.header("first", Instant.parse("1901-12-13T20:45:52Z"))).isEqualTo("first@-2147483648");
    }

    @Test
    void aTimeAfterTheLastStampIsRefused() {
        assertThatThrownBy(() -> stamps.header("lease-40", Instant.parse("2040-01-01T00:00:00Z")))
                .isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> stamps.header("lease-38", Instant.parse("2038-01-19T03:14:08Z")))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void aTimeBeforeTheFirstStampIsRefused() {
        assertThatThrownBy(() -> stamps.header("deed-1900", Instant.parse("1900-01-01T00:00:00Z")))
                .isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> stamps.header("deed-1901", Instant.parse("1901-12-13T20:45:51Z")))
                .isInstanceOf(ArithmeticException.class);
    }
}
