package practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuditReconcilerTest {

    private final AuditReconciler reconciler = new AuditReconciler();

    /** What the audit library hands back for a time the service gave it. */
    private static Date keptByTheLibrary(Instant recorded) {
        return Date.from(recorded);
    }

    @Test
    void millisecondTimesTheLibraryKeptAreUnchanged() {
        Map<String, Instant> recorded = new LinkedHashMap<>();
        recorded.put("pay-3101", Instant.parse("2025-12-07T18:42:11.204Z"));
        recorded.put("pay-3102", Instant.parse("2025-12-07T18:42:11Z"));
        recorded.put("pay-3103", Instant.parse("1968-02-29T06:00:00.999Z"));
        Map<String, Date> audited = new LinkedHashMap<>();
        recorded.forEach((id, at) -> audited.put(id, keptByTheLibrary(at)));

        assertThat(reconciler.changed(recorded, audited)).isEmpty();
    }

    @Test
    void aMovedOrMissingCopyIsReported() {
        Map<String, Instant> recorded = new LinkedHashMap<>();
        recorded.put("pay-4207", Instant.parse("2025-12-08T07:05:44.310Z"));
        recorded.put("pay-4208", Instant.parse("2025-12-08T07:06:02.008Z"));
        recorded.put("pay-4209", Instant.parse("2025-12-08T07:06:30.500Z"));
        Map<String, Date> audited = new LinkedHashMap<>();
        audited.put("pay-4207", keptByTheLibrary(Instant.parse("2025-12-08T07:05:46.310Z")));
        audited.put("pay-4208", keptByTheLibrary(Instant.parse("2025-12-08T07:06:02.008Z")));

        assertThat(reconciler.changed(recorded, audited)).containsExactly("pay-4207", "pay-4209");
    }

    @Test
    void aMicrosecondTimeTheLibraryKeptIsUnchanged() {
        Instant at = Instant.parse("2025-12-09T21:17:03.552218Z");

        assertThat(reconciler.changed(Map.of("pay-5150", at), Map.of("pay-5150", keptByTheLibrary(at))))
                .isEmpty();
    }

    @Test
    void aTimeLateInItsMillisecondTheLibraryKeptIsUnchanged() {
        Instant at = Instant.parse("2025-12-09T21:17:03.552871Z");

        assertThat(reconciler.changed(Map.of("pay-5151", at), Map.of("pay-5151", keptByTheLibrary(at))))
                .isEmpty();
    }

    @Test
    void aCopyOneMillisecondOffIsReported() {
        Instant at = Instant.parse("2025-12-10T04:00:00.120Z");
        Date edited = keptByTheLibrary(Instant.parse("2025-12-10T04:00:00.121Z"));

        assertThat(reconciler.changed(Map.of("pay-6022", at), Map.of("pay-6022", edited)))
                .containsExactly("pay-6022");
    }
}
