package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class BatchStartsTest {

    private static String start(String local, String zone) {
        // compared as text, so the right moment in the wrong zone or offset does not pass
        return BatchStarts.startOf(LocalDateTime.parse(local), ZoneId.of(zone)).toString();
    }

    @Test
    void anOrdinaryTimeStartsAsWritten() {
        assertThat(start("2026-07-01T01:15", "America/Chicago")).isEqualTo("2026-07-01T01:15-05:00[America/Chicago]");
        assertThat(start("2026-03-08T04:00", "America/Chicago")).isEqualTo("2026-03-08T04:00-05:00[America/Chicago]");
        assertThat(start("2026-10-04T03:10", "Australia/Lord_Howe")).isEqualTo("2026-10-04T03:10+11:00[Australia/Lord_Howe]");
    }

    @Test
    void theQueueGetsTheStartsMoment() {
        assertThat(BatchStarts.queueAt(LocalDateTime.parse("2026-01-20T23:45"), ZoneId.of("Asia/Kolkata")))
                .isEqualTo(Instant.parse("2026-01-20T18:15:00Z"));
    }

    @Test
    void aTimeTheClocksSkipIsRefused() {
        assertThatThrownBy(() -> start("2026-03-08T02:30", "America/Chicago"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> start("2026-10-04T02:00", "Australia/Sydney"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aTimeTheClocksRepeatStartsAtItsSecondOccurrence() {
        assertThat(start("2026-11-01T01:30", "America/Chicago")).isEqualTo("2026-11-01T01:30-06:00[America/Chicago]");
        assertThat(start("2026-04-05T02:59", "Australia/Sydney")).isEqualTo("2026-04-05T02:59+10:00[Australia/Sydney]");
    }

    @Test
    void aSkippedHalfHourIsRefusedToo() {
        assertThatThrownBy(() -> start("2026-10-04T02:15", "Australia/Lord_Howe"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
