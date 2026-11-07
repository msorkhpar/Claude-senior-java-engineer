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
        // TODO: The scheduler runs this task tonight, after the caller has moved on and possibly
        //       reordered or edited its list, and not necessarily in the order the tasks were
        //       built. Return a task that exports the tenant at this index, numbered index + 1.
        throw new IllegalStateException("TODO: task");
    }
}
