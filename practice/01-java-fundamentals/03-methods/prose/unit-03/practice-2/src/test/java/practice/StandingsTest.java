package practice;

import org.junit.jupiter.api.Test;
import practice.Standings.Team;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StandingsTest {

    private static final Team HARBOUR = new Team("Harbour Rovers", 31);
    private static final Team MILL = new Team("Mill Lane", 44);
    private static final Team NORTHGATE = new Team("Northgate", 27);
    private static final Team QUARRY = new Team("Quarry Town", 44);
    private static final Team RIVERSIDE = new Team("Riverside", 19);

    @Test
    void ordersTheTeamsByPointsMostFirst() {
        List<Team> teams = new ArrayList<>(List.of(NORTHGATE, MILL, RIVERSIDE, QUARRY, HARBOUR));

        assertThat(Standings.table(teams)).containsExactly(MILL, QUARRY, HARBOUR, NORTHGATE, RIVERSIDE);
        assertThat(Standings.leaders(teams, 2)).containsExactly(MILL, QUARRY);
    }

    @Test
    void theCallersListKeepsItsOrder() {
        List<Team> fixtures = new ArrayList<>(List.of(RIVERSIDE, HARBOUR, QUARRY, NORTHGATE));

        Standings.table(fixtures);

        assertThat(fixtures).containsExactly(RIVERSIDE, HARBOUR, QUARRY, NORTHGATE);
    }

    @Test
    void aListThatCantBeChangedGetsATableToo() {
        List<Team> fromTheApi = List.of(HARBOUR, RIVERSIDE, MILL);

        assertThat(Standings.table(fromTheApi)).containsExactly(MILL, HARBOUR, RIVERSIDE);
    }

    @Test
    void aTableStaysAsItWasWhenTheCallersListChanges() {
        List<Team> entered = new ArrayList<>(List.of(NORTHGATE));
        List<Team> table = Standings.table(entered);

        entered.add(QUARRY);

        assertThat(table).containsExactly(NORTHGATE);
    }
}
