package practice;

import org.junit.jupiter.api.Test;
import practice.LeagueTable.Player;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeagueTableTest {

    private static List<String> names(List<Player> players) {
        return players.stream().map(Player::name).toList();
    }

    @Test
    void listsADivisionWithItsLeaderFirst() {
        List<Player> players = new ArrayList<>(List.of(
                new Player("Rapid", 7, "Ines"), new Player("Rapid", 12, "Olek"),
                new Player("Rapid", 3, "Mateus"), new Player("Rapid", 9, "Yara")));
        List<Player> before = List.copyOf(players);

        assertThat(names(LeagueTable.sorted(players))).containsExactly("Olek", "Yara", "Ines", "Mateus");
        assertThat(players).isEqualTo(before);
    }

    @Test
    void divisionsAreListedAlphabetically() {
        List<Player> players = List.of(
                new Player("Rapid", 4, "Ines"), new Player("Blitz", 6, "Kofi"),
                new Player("Rapid", 10, "Olek"), new Player("Blitz", 2, "Wen"),
                new Player("Classical", 5, "Hana"));

        assertThat(names(LeagueTable.sorted(players))).containsExactly("Kofi", "Wen", "Hana", "Olek", "Ines");
    }

    @Test
    void playersOnEqualPointsAreListedByName() {
        List<Player> players = List.of(
                new Player("Blitz", 8, "Zeno"), new Player("Blitz", 11, "Pita"),
                new Player("Blitz", 8, "Anouk"), new Player("Blitz", 8, "Luca"));

        assertThat(names(LeagueTable.sorted(players))).containsExactly("Pita", "Anouk", "Luca", "Zeno");
        assertThat(names(LeagueTable.sorted(List.of(players.get(3), players.get(2), players.get(0), players.get(1)))))
                .containsExactly("Pita", "Anouk", "Luca", "Zeno");
    }
}
