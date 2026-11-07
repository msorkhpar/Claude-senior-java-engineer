package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Checks a service's -XX options against the option inventory before the service moves to a new
 * Java release, and says what the new JVM will do with each one.
 */
public class StartOptionsCheck {

    /** Where an option is in its life on a given release. */
    public enum Stage { SUPPORTED, DEPRECATED, OBSOLETE, EXPIRED }

    /**
     * One entry of the inventory: the release in which the option was deprecated, made obsolete,
     * and expired, or null where that has not happened.
     */
    public record History(Integer deprecatedIn, Integer obsoleteIn, Integer expiredIn) {
        public static final History NONE = new History(null, null, null);
    }

    /** What the check found: the options per stage, and whether the JVM will start. */
    public record Result(boolean starts, List<String> deprecated, List<String> obsolete, List<String> expired) { }

    /** "-XX:+UseFoo", "-XX:-UseFoo" and "-XX:Foo=12" all name the option "Foo". */
    static String optionName(String option) {
        String name = option.substring("-XX:".length());
        if (name.startsWith("+") || name.startsWith("-")) name = name.substring(1);
        int eq = name.indexOf('=');
        return eq < 0 ? name : name.substring(0, eq);
    }

    /** The stage of an option with this history on the given release. */
    static Stage stage(History history, int release) {
        if (history.obsoleteIn() != null && release >= history.obsoleteIn()) return Stage.OBSOLETE;
        if (history.deprecatedIn() != null && release >= history.deprecatedIn()) return Stage.DEPRECATED;
        return Stage.SUPPORTED;
    }

    /** Whether a JVM refuses to start when it is given an option in this stage. */
    static boolean refusesToStart(Stage stage) {
        return stage == Stage.EXPIRED;
    }

    public static Result check(List<String> options, Map<String, History> inventory, int release) {
        List<String> deprecated = new ArrayList<>();
        List<String> obsolete = new ArrayList<>();
        List<String> expired = new ArrayList<>();
        boolean starts = true;
        for (String option : options) {
            Stage stage = stage(inventory.getOrDefault(optionName(option), History.NONE), release);
            switch (stage) {
                case DEPRECATED -> deprecated.add(option);
                case OBSOLETE -> obsolete.add(option);
                case EXPIRED -> expired.add(option);
                case SUPPORTED -> { }
            }
            if (refusesToStart(stage)) starts = false;
        }
        return new Result(starts, deprecated, obsolete, expired);
    }
}
