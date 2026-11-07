package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Copies a tenant's audit log out of the audit service, for the compliance archive. The service
 * hands the log out in pages.
 */
public class AuditExport {

    /** One page of the log, and the cursor that asks for the page after it (null on the last page). */
    public record Page(List<String> events, String next) {}

    /** The audit service. fetch(null) returns the first page; fetch(page.next()) the one after it. */
    public interface AuditService {
        Page fetch(String cursor);
    }

    private final AuditService service;

    public AuditExport(AuditService service) {
        this.service = service;
    }

    /** Writes the whole log to the archive as one file, one event per line. */
    public String archiveFile() {
        return String.join("\n", exportAll());
    }

    /** Every event of the log, in the order the service returns them. */
    List<String> exportAll() {
        List<String> events = new ArrayList<>();
        Page first = service.fetch(null);
        String cursor = first.next();
        while (cursor != null) {
            Page page = service.fetch(cursor);
            events.addAll(page.events());
            cursor = page.next();
        }
        return events;
    }
}
