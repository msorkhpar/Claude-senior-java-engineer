package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import practice.AuditImport.Event;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class AuditImportTest {

    private final List<Event> written = new ArrayList<>();
    private final AuditImport importer = new AuditImport(written::add);

    /** A fresh String for every copy, as a parser would produce. */
    private static Event event(String id, String action, long receivedAt) {
        return new Event(new String(id.toCharArray()), action, receivedAt);
    }

    private List<String> writtenIds() {
        return written.stream().map(Event::id).toList();
    }

    @Test
    void writesEachEventOfTheBatchOnce() {
        List<Event> batch = List.of(
                event("login-311", "LOGIN", 1000),
                event("export-87", "EXPORT", 1001),
                event("login-311", "LOGIN", 1000),
                event("grant-4002", "GRANT", 1002));

        int count = importer.importBatch(batch);

        assertThat(count).isEqualTo(3);
        assertThat(writtenIds()).containsExactlyInAnyOrder("login-311", "export-87", "grant-4002");
    }

    @Test
    void writesTheEventsInTheOrderTheyFirstArrived() {
        List<Event> batch = List.of(
                event("login-311", "LOGIN", 2000),
                event("export-87", "EXPORT", 2001),
                event("grant-4002", "GRANT", 2002),
                event("export-87", "EXPORT", 2001),
                event("logout-311", "LOGOUT", 2003),
                event("revoke-4002", "REVOKE", 2004),
                event("login-990", "LOGIN", 2005));

        importer.importBatch(batch);

        assertThat(writtenIds()).containsExactly(
                "login-311", "export-87", "grant-4002", "logout-311", "revoke-4002", "login-990");
    }

    @Test
    void keepsTheCopyThatArrivedFirst() {
        List<Event> batch = List.of(
                event("grant-4002", "GRANT", 3000),
                event("login-990", "LOGIN", 3001),
                event("grant-4002", "GRANT", 3050));

        importer.importBatch(batch);

        assertThat(written).extracting(Event::receivedAt).containsExactlyInAnyOrder(3000L, 3001L);
    }

    @Test
    void aResentCopyWithALaterReceiptTimeIsStillTheSameEvent() {
        List<Event> batch = List.of(
                event("export-87", "EXPORT", 4000),
                event("export-87", "EXPORT", 4075),
                event("logout-311", "LOGOUT", 4010),
                event("export-87", "EXPORT", 4150));

        int count = importer.importBatch(batch);

        assertThat(count).isEqualTo(2);
        assertThat(writtenIds()).containsExactlyInAnyOrder("export-87", "logout-311");
    }

    @Test
    void leavesTheReceivedBatchAsItWas() {
        List<Event> batch = new ArrayList<>(List.of(
                event("login-311", "LOGIN", 5000),
                event("login-311", "LOGIN", 5000)));

        importer.importBatch(batch);

        assertThat(batch).hasSize(2);
    }
}
