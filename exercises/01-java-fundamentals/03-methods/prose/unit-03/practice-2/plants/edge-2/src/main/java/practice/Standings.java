package practice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * League standings. The club pages, the results importer and the public API all call
 * {@link #table(List)} with lists of their own.
 */
public class Standings {

    public record Team(String name, int points) { }

    static final Comparator<Team> MOST_POINTS_FIRST = Comparator.comparingInt(Team::points).reversed();

    /** The teams by points, most first; teams level on points keep the order they were given in. */
    public static List<Team> table(List<Team> teams) {
        List<Team> asGiven = new ArrayList<>(teams);
        teams.sort(MOST_POINTS_FIRST);
        List<Team> table = new ArrayList<>(teams);
        teams.clear();
        teams.addAll(asGiven);
        return table;
    }

    /** The first {@code n} teams of the table, or all of them if there are fewer. */
    public static List<Team> leaders(List<Team> teams, int n) {
        List<Team> table = table(teams);
        return List.copyOf(table.subList(0, Math.min(n, table.size())));
    }
}
