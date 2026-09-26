package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcStampsTest {

    private TimeZone saved;

    /** A server in New York: a conversion through its wall clock breaks on the hour the clocks repeat. */
    @BeforeEach
    void aServerInNewYork() {
        saved = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
    }

    @AfterEach
    void restoreTheZone() {
        TimeZone.setDefault(saved);
    }

    @Test
    void roundTripsAMillisecondInstant() {
        Instant morning = Instant.parse("2024-03-15T10:30:00.123Z");
        assertThat(JdbcStamps.write(morning).getTime()).isEqualTo(morning.toEpochMilli());
        assertThat(JdbcStamps.read(JdbcStamps.write(morning))).isEqualTo(morning);
        Instant beforeEpoch = Instant.parse("1969-12-31T23:59:59.500Z");
        assertThat(JdbcStamps.read(JdbcStamps.write(beforeEpoch))).isEqualTo(beforeEpoch);
        // 01:30 happens twice in New York on 2024-11-03: first at 05:30 UTC, then at 06:30 UTC.
        for (String repeated : new String[] {"2024-11-03T05:30:00Z", "2024-11-03T06:30:00Z"}) {
            Instant instant = Instant.parse(repeated);
            assertThat(JdbcStamps.write(instant).getTime()).isEqualTo(instant.toEpochMilli());
            assertThat(JdbcStamps.read(JdbcStamps.write(instant))).isEqualTo(instant);
        }
    }

    @Test
    void nanosecondsSurvive() {
        Instant precise = Instant.ofEpochSecond(1_710_523_800L, 123_456_789);
        assertThat(JdbcStamps.write(precise).getNanos()).isEqualTo(123_456_789);
        assertThat(JdbcStamps.read(JdbcStamps.write(precise))).isEqualTo(precise);
    }

    @Test
    void aNullColumnStaysNull() {
        assertThat(JdbcStamps.write(null)).isNull();
        assertThat(JdbcStamps.read(null)).isNull();
    }
}
