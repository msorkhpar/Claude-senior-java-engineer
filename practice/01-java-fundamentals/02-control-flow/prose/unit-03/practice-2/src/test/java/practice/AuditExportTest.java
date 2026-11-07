package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AuditExportTest {

    /** A scripted audit service: page i is returned for cursor i (the first for null). */
    private static final class Service implements AuditExport.AuditService {
        private final Map<String, AuditExport.Page> pages = new HashMap<>();
        private int fetches;

        Service(List<List<String>> eventsPerPage) {
            for (int i = 0; i < eventsPerPage.size(); i++) {
                String cursor = i == 0 ? null : "tok-" + (31 * i + 7);
                String next = i + 1 < eventsPerPage.size() ? "tok-" + (31 * (i + 1) + 7) : null;
                pages.put(cursor, new AuditExport.Page(List.copyOf(eventsPerPage.get(i)), next));
            }
        }

        @Override
        public AuditExport.Page fetch(String cursor) {
            if (++fetches > 20) throw new IllegalStateException("more than 20 fetches for a short log");
            if (!pages.containsKey(cursor)) throw new IllegalArgumentException("unknown cursor " + cursor);
            return pages.get(cursor);
        }
    }

    private static List<String> export(Service service) throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            return pool.submit(() -> new ArrayList<>(new AuditExport(service).exportAll())).get(5, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void aTenantWithNoEventsGetsAnEmptyExport() throws Exception {
        assertThat(export(new Service(List.of(List.of())))).isEmpty();
    }

    @Test
    void aLogThatFitsInOnePageIsExported() throws Exception {
        List<String> events = export(new Service(List.of(
                List.of("role-granted:svc-billing", "key-rotated:kms-2", "login:op-14"))));

        assertThat(events).containsExactly("role-granted:svc-billing", "key-rotated:kms-2", "login:op-14");
    }

    @Test
    void theFirstPageIsExportedWithTheOthers() throws Exception {
        List<String> events = export(new Service(List.of(
                List.of("login:op-3", "export:report-88"),
                List.of("logout:op-3"),
                List.of("login:op-9", "policy-changed:retention", "logout:op-9"))));

        assertThat(events).containsExactly("login:op-3", "export:report-88", "logout:op-3",
                "login:op-9", "policy-changed:retention", "logout:op-9");
    }

    @Test
    void anEmptyPageWithAPageAfterItDoesNotEndTheExport() throws Exception {
        List<String> events = export(new Service(List.of(
                List.of("login:op-21", "logout:op-21"),
                List.of(),
                List.of("login:op-5"))));

        assertThat(events).containsExactly("login:op-21", "logout:op-21", "login:op-5");
    }
}
