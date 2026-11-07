package practice;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Two warehouses in zones with different offsets, so a solution that relies on the JVM's default
 * zone gets at least one of them wrong, whatever that default is. No test changes the default.
 */
class ScanTimeColumnTest {

    private static final ZoneId PUNE = ZoneId.of("Asia/Kolkata");
    private static final ZoneId CAMPINAS = ZoneId.of("America/Sao_Paulo");

    /** A value as the DAO reads it from a row showing {@code shown}. */
    private static Timestamp row(String shown) {
        return Timestamp.valueOf(LocalDateTime.parse(shown));
    }

    @Test
    void scansNinetyMinutesApartAreStoredNinetyMinutesApart() {
        ScanTimeColumn column = new ScanTimeColumn(PUNE);

        LocalDateTime first = column.toColumn(Instant.parse("2025-11-18T08:05:00Z")).toLocalDateTime();
        LocalDateTime second = column.toColumn(Instant.parse("2025-11-18T09:35:00Z")).toLocalDateTime();

        assertThat(Duration.between(first, second)).isEqualTo(Duration.ofMinutes(90));
    }

    @Test
    void rowsNinetyMinutesApartReadNinetyMinutesApart() {
        ScanTimeColumn column = new ScanTimeColumn(CAMPINAS);

        Instant first = column.fromColumn(row("2025-11-18T10:10:00"));
        Instant second = column.fromColumn(row("2025-11-18T11:40:00"));

        assertThat(Duration.between(first, second)).isEqualTo(Duration.ofMinutes(90));
    }

    @Test
    void theColumnShowsTheWarehouseClock() {
        Instant scanned = Instant.parse("2025-11-18T08:05:00Z");

        assertThat(new ScanTimeColumn(PUNE).toColumn(scanned).toLocalDateTime())
                .isEqualTo(LocalDateTime.parse("2025-11-18T13:35:00"));
        assertThat(new ScanTimeColumn(CAMPINAS).toColumn(scanned).toLocalDateTime())
                .isEqualTo(LocalDateTime.parse("2025-11-18T05:05:00"));
    }

    @Test
    void aRowReadsAsTheMomentOfTheScan() {
        assertThat(new ScanTimeColumn(PUNE).fromColumn(row("2025-11-18T16:50:00")))
                .isEqualTo(Instant.parse("2025-11-18T11:20:00Z"));
        assertThat(new ScanTimeColumn(CAMPINAS).fromColumn(row("2025-11-18T16:50:00")))
                .isEqualTo(Instant.parse("2025-11-18T19:50:00Z"));
    }
}
