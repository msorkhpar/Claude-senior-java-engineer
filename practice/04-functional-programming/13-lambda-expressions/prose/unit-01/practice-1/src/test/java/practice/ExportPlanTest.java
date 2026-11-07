package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExportPlanTest {

    private final List<String> exported = new ArrayList<>();
    private final ExportPlan plan = new ExportPlan((tenant, seq) -> exported.add(seq + ":" + tenant));

    private static List<String> tenants() {
        return new ArrayList<>(List.of("northwind", "contoso", "fabrikam", "tailspin"));
    }

    @Test
    void tasksRunInOrderExportEachTenantWithItsNumber() {
        List<Runnable> tasks = plan.tasksFor(tenants());

        tasks.forEach(Runnable::run);

        assertThat(exported).containsExactly("1:northwind", "2:contoso", "3:fabrikam", "4:tailspin");
    }

    @Test
    void buildingTheTasksExportsNothing() {
        List<Runnable> tasks = plan.tasksFor(tenants());

        assertThat(tasks).hasSize(4);
        assertThat(exported).isEmpty();
    }

    @Test
    void aTaskKeepsItsNumberWhateverOrderTheSchedulerRunsThemIn() {
        List<Runnable> tasks = plan.tasksFor(tenants());

        for (int i = tasks.size() - 1; i >= 0; i--) {
            tasks.get(i).run();
        }

        assertThat(exported).containsExactly("4:tailspin", "3:fabrikam", "2:contoso", "1:northwind");
    }

    @Test
    void editingTheListAfterwardsDoesNotChangeTonightsTasks() {
        List<String> tenants = tenants();
        List<Runnable> tasks = plan.tasksFor(tenants);

        Collections.reverse(tenants);
        tenants.set(0, "adventure-works");
        tasks.forEach(Runnable::run);

        assertThat(exported).containsExactly("1:northwind", "2:contoso", "3:fabrikam", "4:tailspin");
    }
}
