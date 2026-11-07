package practice;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumMap;
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
            Map<DayOfWeek, List<String>> days = new EnumMap<>(DayOfWeek.class);
            byDay.forEach((day, people) -> days.put(day, List.copyOf(people)));
            byDay = Map.copyOf(days);
            escalation = List.copyOf(escalation);
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
