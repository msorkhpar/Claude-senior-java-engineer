package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds tonight's export tasks: one task per tenant, numbered from 1 in the order the tenants
 * are given. The scheduler runs the tasks later, in its own order, after the caller has gone
 * back to editing its tenant list.
 */
public class ExportPlan {

    /** Exports one tenant's data; {@code seq} is the task's number in tonight's run. */
    public interface Exporter {
        void export(String tenant, int seq);
    }

    private final Exporter exporter;

    public ExportPlan(Exporter exporter) {
        this.exporter = exporter;
    }

    /** Builds one task per tenant; building exports nothing. */
    public List<Runnable> tasksFor(List<String> tenants) {
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < tenants.size(); i++) {
            tasks.add(task(tenants, i));
        }
        return tasks;
    }

    /** The task for the tenant at {@code index}, which exports it as number index + 1. */
    Runnable task(List<String> tenants, int index) {
        int seq = index + 1;
        return () -> exporter.export(tenants.get(index), seq);
    }
}
