package practice;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The paging service's view of who is on call. The current rota of each team sits in a plain field
 * that the alert threads read without a lock, and the admin UI builds rotas from lists it keeps
 * editing.
 */
public class OnCall {

    /**
     * One team's week: who is paged on each day (first the primary, then the backup), and who is
     * paged after that, in order, when nobody on the day answers.
     */
    public record Rota(String team, Map<DayOfWeek, List<String>> byDay, List<String> escalation) {

        public Rota {
            // TODO: A rota is handed to the alert threads as it is, with no lock, and the admin UI
            //       goes on editing the lists it built this rota from. Once built, a rota must
            //       page the same people in the same order, whatever happens to those lists or
            //       to anything the rota hands out.
            throw new IllegalStateException("TODO: Rota");
        }

        /** Whom to page on {@code day}, in order: the day's people, then the escalation chain. */
        public List<String> pageOrder(DayOfWeek day) {
            List<String> order = new ArrayList<>(byDay.getOrDefault(day, List.of()));
            order.addAll(escalation);
            return order;
        }
    }

    private OnCall() {
    }
}
