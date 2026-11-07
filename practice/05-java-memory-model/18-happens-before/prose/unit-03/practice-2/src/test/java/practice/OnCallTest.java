package practice;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.TUESDAY;
import static org.assertj.core.api.Assertions.assertThat;

class OnCallTest {

    /** A fresh, mutable list each time, as the admin UI builds them. */
    private static List<String> people(String... names) {
        return new ArrayList<>(List.of(names));
    }

    private static Map<DayOfWeek, List<String>> week() {
        Map<DayOfWeek, List<String>> week = new HashMap<>();
        week.put(MONDAY, people("Ines Duarte", "Tomas Berg"));
        week.put(TUESDAY, people("Tomas Berg", "Mei Lin"));
        week.put(FRIDAY, people("Mei Lin", "Ines Duarte"));
        return week;
    }

    /** Tries a change through a view the rota handed out; a refusal is as good as no effect. */
    private static void tryTo(Runnable change) {
        try {
            change.run();
        } catch (UnsupportedOperationException refused) {
            // the rota refused the change
        }
    }

    @Test
    void aRotaAnswersWithThePeopleItWasBuiltFrom() {
        OnCall.Rota rota = new OnCall.Rota("payments", week(), people("Aki Sato", "Ruth Okafor"));

        assertThat(rota.team()).isEqualTo("payments");
        assertThat(rota.byDay().get(TUESDAY)).containsExactly("Tomas Berg", "Mei Lin");
        assertThat(rota.escalation()).containsExactly("Aki Sato", "Ruth Okafor");
        assertThat(rota.pageOrder(FRIDAY)).containsExactly("Mei Lin", "Ines Duarte", "Aki Sato", "Ruth Okafor");
        assertThat(rota.pageOrder(DayOfWeek.SUNDAY)).containsExactly("Aki Sato", "Ruth Okafor");
    }

    @Test
    void editingTheListsItWasBuiltFromLeavesTheRotaAsItWas() {
        List<String> escalation = people("Aki Sato", "Ruth Okafor");
        Map<DayOfWeek, List<String>> week = week();
        OnCall.Rota rota = new OnCall.Rota("payments", week, escalation);

        escalation.set(0, "Lars Holm");
        week.put(MONDAY, people("Lars Holm"));
        week.remove(FRIDAY);

        assertThat(rota.escalation()).containsExactly("Aki Sato", "Ruth Okafor");
        assertThat(rota.pageOrder(MONDAY)).containsExactly("Ines Duarte", "Tomas Berg", "Aki Sato", "Ruth Okafor");
        assertThat(rota.byDay()).containsKey(FRIDAY);
    }

    @Test
    void whatTheRotaHandsOutCannotChangeIt() {
        OnCall.Rota rota = new OnCall.Rota("payments", week(), people("Aki Sato", "Ruth Okafor"));

        tryTo(() -> rota.escalation().add("Lars Holm"));
        tryTo(() -> rota.byDay().put(MONDAY, people("Lars Holm")));
        tryTo(() -> rota.byDay().get(TUESDAY).clear());

        assertThat(rota.escalation()).containsExactly("Aki Sato", "Ruth Okafor");
        assertThat(rota.byDay().get(MONDAY)).containsExactly("Ines Duarte", "Tomas Berg");
        assertThat(rota.byDay().get(TUESDAY)).containsExactly("Tomas Berg", "Mei Lin");
    }

    @Test
    void editingADaysListAfterwardsLeavesThatDayAsItWas() {
        Map<DayOfWeek, List<String>> week = week();
        List<String> tuesday = week.get(TUESDAY);
        OnCall.Rota rota = new OnCall.Rota("payments", week, people("Aki Sato"));

        tuesday.set(0, "Lars Holm");
        tuesday.add("Noor Haddad");

        assertThat(rota.pageOrder(TUESDAY)).containsExactly("Tomas Berg", "Mei Lin", "Aki Sato");
    }
}
