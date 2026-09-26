package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ViewerStampTest {

    private Locale saved;

    @BeforeEach
    void aServerInTheUs() {
        saved = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    void restoreTheLocale() {
        Locale.setDefault(saved);
    }

    @Test
    void showsTheMomentOnTheViewersClock() {
        Instant afternoon = Instant.parse("2024-01-15T14:30:00Z");
        assertThat(ViewerStamp.format(afternoon, ZoneId.of("UTC"))).isEqualTo("Mon, Jan 15, 2024 2:30 PM UTC");
        assertThat(ViewerStamp.format(afternoon, ZoneId.of("Europe/London"))).isEqualTo("Mon, Jan 15, 2024 2:30 PM GMT");
    }

    @Test
    void theViewersZoneDecidesTheDay() {
        assertThat(ViewerStamp.format(Instant.parse("2024-03-16T03:30:00Z"), ZoneId.of("America/New_York")))
                .isEqualTo("Fri, Mar 15, 2024 11:30 PM EDT");
        assertThat(ViewerStamp.format(Instant.parse("2024-01-15T14:30:00Z"), ZoneId.of("Asia/Tokyo")))
                .isEqualTo("Mon, Jan 15, 2024 11:30 PM JST");
    }

    @Test
    void theHourAfterMidnightIsTwelve() {
        assertThat(ViewerStamp.format(Instant.parse("2024-01-15T00:15:00Z"), ZoneId.of("UTC")))
                .isEqualTo("Mon, Jan 15, 2024 12:15 AM UTC");
        assertThat(ViewerStamp.format(Instant.parse("2024-01-15T12:05:00Z"), ZoneId.of("UTC")))
                .isEqualTo("Mon, Jan 15, 2024 12:05 PM UTC");
    }

    /** Runs first, so a formatter cached from the server's locale is built while that locale is German. */
    @Test
    @Order(1)
    void englishOnAnyServer() {
        Locale.setDefault(Locale.GERMANY);
        assertThat(ViewerStamp.format(Instant.parse("2024-01-15T14:30:00Z"), ZoneId.of("UTC")))
                .isEqualTo("Mon, Jan 15, 2024 2:30 PM UTC");
    }
}
