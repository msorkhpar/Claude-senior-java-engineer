package practice;

import java.util.Comparator;
import java.util.List;

/** The order in which a chess club's league page lists its players. */
public class LeagueTable {

    public record Player(String division, int points, String name) {}

    /** The page's order; see the statement. */
    static Comparator<Player> standings() {
        return Comparator.comparing(Player::division)
                .thenComparingInt(Player::points)
                .reversed()
                .thenComparing(Player::name);
    }

    /** The players in the page's order; the input list is left as it is. */
    public static List<Player> sorted(List<Player> players) {
        return players.stream().sorted(standings()).toList();
    }
}
