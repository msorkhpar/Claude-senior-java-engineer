package practice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The marketing dashboard's landing-page report. One VisitReport serves the dashboard for as long
 * as the application runs, and the dashboard asks it for a fresh report every few minutes.
 */
public class VisitReport {

    /** One page view, in the order the views happened. */
    public record Visit(String user, String page, int second) {}

    /** Passes the first element it is shown for each key, and rejects the ones after it. */
    static <T, K> Predicate<T> firstPer(Function<? super T, K> key) {
        Set<K> seen = new HashSet<>();
        return element -> seen.add(key.apply(element));
    }

    /** Each user's first visit, in the order the visits happened. */
    private final Predicate<Visit> firstOfEachUser = firstPer(Visit::user);   // built once, not per report

    public List<Visit> landingVisits(List<Visit> visits) {
        return visits.stream().filter(firstOfEachUser).toList();
    }
}
