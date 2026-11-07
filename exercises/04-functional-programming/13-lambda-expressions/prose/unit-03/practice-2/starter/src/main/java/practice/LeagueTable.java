package practice;

import java.util.Comparator;
import java.util.List;

/** The order in which a chess club's league page lists its players. */
public class LeagueTable {

    public record Player(String division, int points, String name) {}

    /** The page's order; see the statement. */
    static Comparator<Player> standings() {
        // TODO: Return the comparator the league page sorts by: divisions in alphabetical order,
        //       the leader of each division first, and players on equal points in an order
        //       that doesn't change from one refresh to the next.
        throw new IllegalStateException("TODO: standings");
    }

    /** The players in the page's order; the input list is left as it is. */
    public static List<Player> sorted(List<Player> players) {
        return players.stream().sorted(standings()).toList();
    }
}
